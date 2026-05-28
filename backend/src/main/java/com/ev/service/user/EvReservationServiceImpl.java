package com.ev.service.user;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ev.dao.user.EvReservationDAO;
import com.ev.dto.reservation.EvReservationChargerDTO;
import com.ev.dto.reservation.EvReservationDTO;
import com.ev.dto.vehicle.EvVehicleDTO;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/*
 * 사용자 예약 관련 Service 구현체
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EvReservationServiceImpl implements EvReservationService {

    private final EvReservationDAO reservationDAO;
    private final EvChargerRedisService evChargerRedisService;

    /*
     * 인증 실패 허용 횟수
     */
    private static final long MAX_VERIFY_ATTEMPT = 5L;

    /*
     * 예약 폼에서 보여줄 충전기 정보 조회
     */
    @Override
    public EvReservationChargerDTO getReservationCharger(Long chargerId) {
        log.info("@# ReservationServiceImpl.getReservationCharger()");
        log.info("@# chargerId => {}", chargerId);

        EvReservationChargerDTO charger = reservationDAO.findReservationChargerById(chargerId);

        if (charger == null) {
            throw new IllegalArgumentException("존재하지 않는 충전기입니다.");
        }

        return charger;
    }

    /*
     * 로그인한 회원의 차량 목록 조회
     */
    @Override
    public List<EvVehicleDTO> getVehicleList(Long memberId) {
        log.info("@# ReservationServiceImpl.getVehicleList()");
        log.info("@# memberId => {}", memberId);

        return reservationDAO.findVehicleListByMemberId(memberId);
    }

    /*
     * 예약 등록
     *
     * 처리 내용:
     * 1. 기본 입력값 검증
     * 2. 예약 날짜 세팅
     * 3. 차량 정보 조회
     * 4. 충전기 정보 조회
     * 5. 예약 시간 중복 체크
     * 6. 예상 충전량 / 예상 시간 / 예상 금액 계산
     * 7. 예약 상태 세팅
     * 8. 예약 insert
     *
     * 중요:
     * - 인증코드는 reservation 테이블에 저장하지 않는다.
     * - 현장 인증코드는 Redis에서 충전기별 임시 코드로 관리한다.
     */
    @Override
    @Transactional
    public Long createReservation(EvReservationDTO reservationDTO) {
        log.info("@# ReservationServiceImpl.createReservation()");
        log.info("@# reservationDTO => {}", reservationDTO);

        /*
         * 1. 기본 입력값 검증
         */
        if (reservationDTO.getMemberId() == null) {
            throw new IllegalArgumentException("로그인이 필요합니다.");
        }

        if (reservationDTO.getVehicleId() == null) {
            throw new IllegalArgumentException("차량을 선택하세요.");
        }

        if (reservationDTO.getChargerId() == null) {
            throw new IllegalArgumentException("충전기를 선택하세요.");
        }

        /*
         * startTime, endTime은 LocalDateTime이다.
         */
        if (reservationDTO.getStartTime() == null || reservationDTO.getEndTime() == null) {
            throw new IllegalArgumentException("예약 시간을 선택하세요.");
        }

        /*
         * 종료 시간이 시작 시간보다 늦어야 한다.
         */
        if (!reservationDTO.getStartTime().isBefore(reservationDTO.getEndTime())) {
            throw new IllegalArgumentException("종료 시간은 시작 시간보다 늦어야 합니다.");
        }

        /*
         * reservation_date는 예약 시작 날짜 기준으로 저장한다.
         */
        reservationDTO.setReservationDate(reservationDTO.getStartTime().toLocalDate());

        if (reservationDTO.getCurrentSoc() == null || reservationDTO.getTargetSoc() == null) {
            throw new IllegalArgumentException("배터리 잔량을 입력하세요.");
        }

        if (reservationDTO.getCurrentSoc() < 0 || reservationDTO.getCurrentSoc() > 100) {
            throw new IllegalArgumentException("현재 배터리 잔량은 0~100 사이여야 합니다.");
        }

        if (reservationDTO.getTargetSoc() < 0 || reservationDTO.getTargetSoc() > 100) {
            throw new IllegalArgumentException("목표 배터리 잔량은 0~100 사이여야 합니다.");
        }

        if (reservationDTO.getCurrentSoc() >= reservationDTO.getTargetSoc()) {
            throw new IllegalArgumentException("목표 배터리 잔량은 현재 배터리보다 커야 합니다.");
        }

        /*
         * 2. 차량 정보 조회
         */
        EvVehicleDTO vehicle = reservationDAO.findVehicleById(
                reservationDTO.getVehicleId(),
                reservationDTO.getMemberId()
        );

        if (vehicle == null) {
            throw new IllegalArgumentException("선택한 차량 정보를 찾을 수 없습니다.");
        }

        /*
         * 3. 충전기 정보 조회
         */
        EvReservationChargerDTO charger = reservationDAO.findReservationChargerById(
                reservationDTO.getChargerId()
        );

        if (charger == null) {
            throw new IllegalArgumentException("선택한 충전기 정보를 찾을 수 없습니다.");
        }

        if (!"사용가능".equals(charger.getChargerStatus())) {
            throw new IllegalArgumentException("현재 예약 가능한 충전기가 아닙니다.");
        }

        /*
         * 4. 예약 시간 중복 체크
         */
        int overlapCount = reservationDAO.countReservationOverlap(
                reservationDTO.getChargerId(),
                reservationDTO.getStartTime(),
                reservationDTO.getEndTime()
        );

        if (overlapCount > 0) {
            throw new IllegalArgumentException("이미 해당 시간에 예약이 존재합니다.");
        }

        /*
         * 5. 예상 충전량 계산
         */
        double batteryCapacity = vehicle.getBatteryCapacityKwh();

        double socDiff = reservationDTO.getTargetSoc() - reservationDTO.getCurrentSoc();

        double requiredKwh = batteryCapacity * (socDiff / 100.0);

        requiredKwh = Math.round(requiredKwh * 100.0) / 100.0;

        /*
         * 6. 예상 충전 시간 계산
         */
        double estimatedMinutesDouble = requiredKwh / charger.getChargingSpeedKw() * 60;

        int estimatedMinutes = (int) Math.ceil(estimatedMinutesDouble);

        /*
         * 7. 예상 금액 계산
         */
        double estimatedCost = requiredKwh * charger.getPricePerKwh();

        estimatedCost = Math.round(estimatedCost);

        /*
         * 8. 예약 기본값 세팅
         */
        reservationDTO.setRequiredKwh(requiredKwh);
        reservationDTO.setEstimatedMinutes(estimatedMinutes);
        reservationDTO.setEstimatedCost(estimatedCost);
        reservationDTO.setStatus("예약완료");

        /*
         * Redis 방식으로 변경했기 때문에
         * reservation.auth_code는 생성하지 않는다.
         */

        /*
         * 9. 예약 등록
         */
        reservationDAO.insertReservation(reservationDTO);

        log.info("@# created reservationId => {}", reservationDTO.getReservationId());

        return reservationDTO.getReservationId();
    }

    /*
     * 예약 완료 정보 조회
     */
    @Override
    public EvReservationDTO getReservationComplete(Long reservationId, Long memberId) {
        log.info("@# ReservationServiceImpl.getReservationComplete()");
        log.info("@# reservationId => {}", reservationId);
        log.info("@# memberId => {}", memberId);

        return reservationDAO.findReservationById(reservationId, memberId);
    }

    /*
     * 내 예약 목록 조회
     */
    @Override
    public List<EvReservationDTO> getMyReservationList(Long memberId) {
        log.info("@# ReservationServiceImpl.getMyReservationList()");
        log.info("@# memberId => {}", memberId);

        return reservationDAO.findReservationListByMemberId(memberId);
    }

    /*
     * 예약 취소
     */
    @Override
    @Transactional
    public void cancelReservation(Long reservationId, Long memberId) {
        log.info("@# EvReservationServiceImpl.cancelReservation()");
        log.info("@# reservationId => {}", reservationId);
        log.info("@# memberId => {}", memberId);

        int updateCount = reservationDAO.cancelReservation(reservationId, memberId);

        if (updateCount == 0) {
            throw new IllegalArgumentException("취소할 수 없는 예약입니다.");
        }
    }

    /*
     * 예약 현장 인증코드 발급
     *
     * 처리 흐름:
     * 1. DB에서 본인 예약 + 예약완료 상태 + 예약 시간 확인
     * 2. 예약의 chargerId 확인
     * 3. Redis에 ev:charger:{chargerId}:auth-code 저장
     * 4. 발급된 인증코드 반환
     */
    @Override
    public String issueAuthCode(Long reservationId, Long memberId) {
        log.info("@# EvReservationServiceImpl.issueAuthCode()");
        log.info("@# reservationId => {}", reservationId);
        log.info("@# memberId => {}", memberId);

        EvReservationDTO reservation =
                reservationDAO.findVerifiableReservation(reservationId, memberId);

        if (reservation == null) {
            throw new IllegalArgumentException("예약 시작 10분 전부터 인증코드를 발급할 수 있습니다.");
        }

        String authCode =
                evChargerRedisService.generateAuthCode(reservation.getChargerId());

        log.info("@# issued authCode => {}", authCode);

        return authCode;
    }

    /*
     * 예약 현장 인증
     *
     * 처리 흐름:
     * 1. 인증 실패 횟수 확인
     * 2. DB에서 본인 예약 + 예약완료 상태 + 예약 시간 확인
     * 3. 예약의 chargerId 확인
     * 4. Redis에서 ev:charger:{chargerId}:auth-code 조회
     * 5. 입력 코드와 Redis 코드 비교
     * 6. 맞으면 reservation.status = '인증완료'
     * 7. Redis charger status = VERIFIED
     * 8. 인증 실패 횟수 초기화
     * 9. 인증코드 삭제로 재사용 방지
     */
    @Override
    @Transactional
    public void verifyReservation(Long reservationId,
                                  Long memberId,
                                  String authCode) {
        log.info("@# EvReservationServiceImpl.verifyReservation()");
        log.info("@# reservationId => {}", reservationId);
        log.info("@# memberId => {}", memberId);
        log.info("@# authCode => {}", authCode);

        /*
         * 1. 인증 실패 횟수 제한 확인
         */
        Long attemptCount = evChargerRedisService.getVerifyAttempt(memberId, reservationId);

        if (attemptCount >= MAX_VERIFY_ATTEMPT) {
            throw new IllegalArgumentException("인증 시도 횟수가 초과되었습니다. 잠시 후 다시 시도해주세요.");
        }

        /*
         * 2. 본인 예약 + 예약 상태 + 예약 시간 확인
         */
        EvReservationDTO reservation =
                reservationDAO.findVerifiableReservation(reservationId, memberId);

        if (reservation == null) {
            evChargerRedisService.increaseVerifyAttempt(memberId, reservationId);
            throw new IllegalArgumentException("인증할 수 없는 예약입니다.");
        }

        /*
         * 3. Redis에서 예약한 충전기의 현재 인증코드 조회
         */
        String savedAuthCode =
                evChargerRedisService.getAuthCode(reservation.getChargerId());

        if (savedAuthCode == null) {
            evChargerRedisService.increaseVerifyAttempt(memberId, reservationId);
            throw new IllegalArgumentException("인증코드가 만료되었습니다. 도착 인증을 다시 진행해주세요.");
        }

        /*
         * 4. 입력 코드와 Redis 코드 비교
         */
        if (!savedAuthCode.equals(authCode)) {
            evChargerRedisService.increaseVerifyAttempt(memberId, reservationId);
            throw new IllegalArgumentException("인증코드가 올바르지 않습니다.");
        }

        /*
         * 5. 인증 성공 처리
         */
        int updateCount =
                reservationDAO.updateReservationStatusToVerified(reservationId, memberId);

        if (updateCount == 0) {
            throw new IllegalArgumentException("예약 인증 처리에 실패했습니다.");
        }

        /*
         * 6. Redis 충전기 현재 상태 변경
         */
        evChargerRedisService.setChargerStatus(
                reservation.getChargerId(),
                "VERIFIED"
        );

        /*
         * 7. 인증 성공 후 실패 횟수 초기화
         */
        evChargerRedisService.clearVerifyAttempt(memberId, reservationId);

        /*
         * 8. 인증 성공 후 인증코드 재사용 방지
         */
        evChargerRedisService.deleteAuthCode(reservation.getChargerId());
    }
    
    @Override
    @Transactional
    public int updateReservationStatusAutomatically() {
        log.info("@# EvReservationServiceImpl.updateReservationStatusAutomatically()");

        int noShowCount = reservationDAO.updateReservationCompleteToNoShow();

        int authenticatedCompleteCount = reservationDAO.updateAuthenticatedToComplete();

        int chargingCount = reservationDAO.updateAuthenticatedToCharging();

        int completeCount = reservationDAO.updateChargingToComplete();

        int totalCount = noShowCount + authenticatedCompleteCount + chargingCount + completeCount;

        log.info("@# noShowCount => {}", noShowCount);
        log.info("@# authenticatedCompleteCount => {}", authenticatedCompleteCount);
        log.info("@# chargingCount => {}", chargingCount);
        log.info("@# completeCount => {}", completeCount);
        log.info("@# total reservation status update count => {}", totalCount);

        return totalCount;
    }
}
