package com.ev.service.user;

import java.time.LocalDateTime;
import java.util.List;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ev.dao.user.EvReservationDAO;
import com.ev.dto.reservation.EvReservationChargerDTO;
import com.ev.dto.reservation.EvReservationDTO;
import com.ev.dto.station.EvChargerDTO;
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
    private final JavaMailSender mailSender;
    
    /*
     * 인증 실패 허용 횟수
     */
    private static final long MAX_VERIFY_ATTEMPT = 5L;

    /*
     * 예약 선점/중복 체크 여유 시간
     * 예약 종료 예상 시간 이후 5분까지 같은 충전기 시간대를 막는다.
     */
    private static final long RESERVATION_SLOT_BUFFER_MINUTES = 5L;

    /*
     * 예약 가능한 충전소/충전기 상태인지 공통으로 확인한다.
     *
     * 기준:
     * - 충전소는 운영중이어야 한다.
     * - 충전기는 사용가능이어야 한다.
     *
     * 점검중/고장/운영중지 상태는 프론트에서 버튼을 막더라도
     * 백엔드에서 한 번 더 차단해야 안전하다.
     */
    private boolean isReservableStationAndCharger(EvReservationChargerDTO charger) {
        if (charger == null) {
            return false;
        }

        boolean stationOpen = "운영중".equals(charger.getStationStatus());
        boolean chargerAvailable = "사용가능".equals(charger.getChargerStatus());

        log.info("@# stationStatus => {}", charger.getStationStatus());
        log.info("@# chargerStatus => {}", charger.getChargerStatus());
        log.info("@# reservable => {}", stationOpen && chargerAvailable);

        return stationOpen && chargerAvailable;
    }

    private String buildNotReservableMessage(EvReservationChargerDTO charger) {
        if (charger == null) {
            return "선택한 충전기 정보를 찾을 수 없습니다.";
        }

        if (!"운영중".equals(charger.getStationStatus())) {
            return "현재 운영중인 충전소가 아니므로 예약할 수 없습니다.";
        }

        if ("점검중".equals(charger.getChargerStatus())) {
            return "현재 점검중인 충전기입니다. 다른 충전기를 선택해 주세요.";
        }

        if ("고장".equals(charger.getChargerStatus())) {
            return "현재 고장 상태인 충전기입니다. 다른 충전기를 선택해 주세요.";
        }

        return "현재 예약 가능한 충전기가 아닙니다.";
    }

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
     * 5. 충전기 예약 시간 중복 체크
     * 6. 차량 예약 시간 중복 체크
     * 7. 예상 충전량 / 예상 시간 / 예상 금액 계산
     * 8. 예약 상태 세팅
     * 9. 예약 insert
     *
     * 중요:
     * - 인증코드는 예약 생성 즉시 DB와 Redis에 저장한다.
     * - 실제 인증은 예약 시작 5분 전부터 예약 시작 5분 후까지만 허용한다.
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
         * 현재 시간보다 이전 시간으로 예약할 수 없다.
         */
        LocalDateTime now = LocalDateTime.now();

        if (reservationDTO.getStartTime().isBefore(now)) {
            throw new IllegalArgumentException("현재 시간보다 이전 시간으로 예약할 수 없습니다.");
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

        if (!isReservableStationAndCharger(charger)) {
            throw new IllegalArgumentException(buildNotReservableMessage(charger));
        }

        /*
         * 4. Redis 시간 구간 임시 선점 소유자 확인
         *
         * 선점 기준:
         * - 충전기 + 예약 시작 시간 + 예상 종료 시간(+5분)
         * - 충전기 전체가 아니라 선택한 시간 구간만 선점한다.
         */
        LocalDateTime holdEndTime = reservationDTO.getEndTime().plusMinutes(RESERVATION_SLOT_BUFFER_MINUTES);

        boolean holdOwner =
                evChargerRedisService.isReservationTimeSlotHoldOwner(
                        reservationDTO.getChargerId(),
                        reservationDTO.getMemberId(),
                        reservationDTO.getStartTime(),
                        holdEndTime
                );

        if (!holdOwner) {
            throw new IllegalArgumentException("선택한 시간대의 임시 선점 시간이 만료되었습니다. 예약 가능 여부를 다시 확인해 주세요.");
        }

        /*
         * 5. 충전기 예약 시간 중복 체크
         *
         * 같은 충전기의 기존 예약 종료 시간 + 5분까지는 겹치는 예약으로 본다.
         */
        int overlapCount = reservationDAO.countReservationOverlap(
                reservationDTO.getChargerId(),
                reservationDTO.getStartTime(),
                holdEndTime
        );

        if (overlapCount > 0) {
            throw new IllegalArgumentException("이미 해당 시간에 예약이 존재합니다.");
        }

        /*
         * 6. 차량 예약 시간 중복 체크
         *
         * 같은 차량이 같은 시간대에
         * 여러 충전기를 동시에 예약하는 것을 막는다.
         */
        int vehicleOverlapCount = reservationDAO.countVehicleReservationOverlap(
                reservationDTO.getVehicleId(),
                reservationDTO.getStartTime(),
                holdEndTime
        );

        if (vehicleOverlapCount > 0) {
            throw new IllegalArgumentException("선택한 차량은 해당 시간대에 이미 예약이 있습니다.");
        }

        /*
         * 7. 예상 충전량 계산
         */
        double batteryCapacity = vehicle.getBatteryCapacityKwh();

        double socDiff = reservationDTO.getTargetSoc() - reservationDTO.getCurrentSoc();

        double requiredKwh = batteryCapacity * (socDiff / 100.0);

        requiredKwh = Math.round(requiredKwh * 100.0) / 100.0;

        /*
         * 8. 예상 충전 시간 계산
         */
        double estimatedMinutesDouble = requiredKwh / charger.getChargingSpeedKw() * 60;

        int estimatedMinutes = (int) Math.ceil(estimatedMinutesDouble);

        /*
         * 9. 예상 금액 계산
         */
        double estimatedCost = requiredKwh * charger.getPricePerKwh();

        estimatedCost = Math.round(estimatedCost);

        /*
         * 10. 예약 기본값 세팅
         */
        reservationDTO.setRequiredKwh(requiredKwh);
        reservationDTO.setEstimatedMinutes(estimatedMinutes);
        reservationDTO.setEstimatedCost(estimatedCost);
        reservationDTO.setStatus("예약완료");

        /*
         * 인증코드는 예약 완료 즉시 생성한다.
         * 사용자는 내 예약 상세에서 언제든 확인할 수 있지만,
         * 실제 인증은 예약 시작 5분 전 ~ 예약 시작 5분 후까지만 가능하다.
         */

        /*
         * 11. 예약 등록
         */
        reservationDAO.insertReservation(reservationDTO);

        LocalDateTime authCodeExpiresAt = reservationDTO.getStartTime().plusMinutes(5);
        String authCode = evChargerRedisService.generateReservationAuthCode(
                reservationDTO.getReservationId(),
                reservationDTO.getChargerId(),
                authCodeExpiresAt
        );

        reservationDTO.setAuthCode(authCode);
        reservationDAO.updateReservationAuthCode(
                reservationDTO.getReservationId(),
                reservationDTO.getMemberId(),
                authCode
        );

        /*
         * 예약 생성 후 실제 예약 점유 상태를 충전기 상태에 반영한다.
         * 공공데이터 적재에서는 예약중을 만들지 않고, 실제 예약 생성 시에만 예약중으로 변경한다.
         */
        reservationDAO.updateChargerStatus(reservationDTO.getChargerId(), "예약중");

        /*
         * 12. 예약 등록 성공 후 Redis 임시 점유 해제
         *
         * 이제 실제 예약 데이터가 DB에 들어갔기 때문에
         * "선택중" Redis key는 유지할 필요가 없다.
         * 이후 화면 상태는 DB 예약 상태를 기준으로 "예약중"으로 판단한다.
         */
        evChargerRedisService.releaseAllReservationTimeSlotHolds(reservationDTO.getMemberId());
        evChargerRedisService.releaseChargerReservationHold(
                reservationDTO.getChargerId(),
                reservationDTO.getMemberId()
        );

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

    @Override
    public EvReservationDTO getMyReservationDetail(Long reservationId, Long memberId) {
        log.info("@# EvReservationServiceImpl.getMyReservationDetail()");
        log.info("@# reservationId => {}", reservationId);
        log.info("@# memberId => {}", memberId);

        EvReservationDTO reservation = reservationDAO.findReservationById(reservationId, memberId);

        if (reservation == null) {
            throw new IllegalArgumentException("예약 정보를 찾을 수 없습니다.");
        }

        return reservation;
    }

    /*
     * 내 예약 목록 조회
     */
    @Override
    public List<EvReservationDTO> getMyReservationList(Long memberId,
                                                       LocalDateTime startDate,
                                                       LocalDateTime endDate) {
        log.info("@# EvReservationServiceImpl.getMyReservationList()");
        log.info("@# memberId => {}", memberId);
        log.info("@# startDate => {}", startDate);
        log.info("@# endDate => {}", endDate);

        return reservationDAO.getMyReservationList(memberId, startDate, endDate);
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

        EvReservationDTO cancelTarget = reservationDAO.findReservationById(reservationId, memberId);

        int updateCount = reservationDAO.cancelReservation(reservationId, memberId);

        if (updateCount == 0) {
            throw new IllegalArgumentException("취소할 수 없는 예약입니다.");
        }

        if (cancelTarget != null && cancelTarget.getChargerId() != null) {
            reservationDAO.updateChargerStatus(cancelTarget.getChargerId(), "사용가능");
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

        EvReservationDTO reservation = reservationDAO.findReservationById(reservationId, memberId);

        if (reservation == null) {
            throw new IllegalArgumentException("예약 정보를 찾을 수 없습니다.");
        }

        if (!"예약완료".equals(reservation.getStatus()) && !"인증완료".equals(reservation.getStatus())) {
            throw new IllegalArgumentException("인증코드는 예약완료 또는 인증완료 상태에서만 확인할 수 있습니다.");
        }

        if (reservation.getAuthCode() != null && !reservation.getAuthCode().isBlank()) {
            log.info("@# return db authCode => {}", reservation.getAuthCode());
            return reservation.getAuthCode();
        }

        LocalDateTime authCodeExpiresAt = reservation.getStartTime().plusMinutes(5);
        String authCode = evChargerRedisService.generateReservationAuthCode(
                reservationId,
                reservation.getChargerId(),
                authCodeExpiresAt
        );

        reservationDAO.updateReservationAuthCode(reservationId, memberId, authCode);

        log.info("@# reissued authCode => {}", authCode);

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

        if (attemptCount != null && attemptCount >= MAX_VERIFY_ATTEMPT) {
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
        String savedAuthCode = evChargerRedisService.getReservationAuthCode(reservationId);

        if (savedAuthCode == null) {
            savedAuthCode = evChargerRedisService.getAuthCode(reservation.getChargerId());
        }

        if (savedAuthCode == null && reservation.getAuthCode() != null) {
            savedAuthCode = reservation.getAuthCode();
        }

        if (savedAuthCode == null) {
            evChargerRedisService.increaseVerifyAttempt(memberId, reservationId);
            throw new IllegalArgumentException("인증코드가 만료되었습니다. 예약 시간과 인증 가능 시간을 확인해주세요.");
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
        reservationDAO.updateChargerStatus(reservation.getChargerId(), "사용중");

        /*
         * 7. 인증 성공 후 실패 횟수 초기화
         */
        evChargerRedisService.clearVerifyAttempt(memberId, reservationId);

        /*
         * 8. 인증 성공 후 인증코드 재사용 방지
         */
        evChargerRedisService.deleteAuthCode(reservation.getChargerId());
        evChargerRedisService.deleteReservationAuthCode(reservationId);

        /*
         * 인증 직후 세션을 바로 만들어 둔다.
         * 이렇게 해야 프론트의 충전 게이지 시뮬레이션에서 실제 충전 시작 시간을 표시할 수 있다.
         */
        reservationDAO.upsertChargingSessionForSimulation(reservationId, memberId);
    }

    /*
     * 충전 시작 시뮬레이션 완료 처리
     *
     * 핵심 설계:
     * - reservation.start_time/end_time은 예약 당시 예정 시간으로 보존한다.
     * - charging_session.actual_start_time은 인증 성공 시각을 사용한다.
     * - charging_session.actual_end_time은 충전 게이지 완료 API가 호출된 서버 현재 시각을 사용한다.
     * - 화면 게이지는 시연을 위해 20초로 고정하고, DB도 그 완료 시각을 실제 완료 시각으로 저장한다.
     */
    @Override
    @Transactional
    public EvReservationDTO completeChargingSimulation(Long reservationId, Long memberId) {
        log.info("@# EvReservationServiceImpl.completeChargingSimulation()");
        log.info("@# reservationId => {}", reservationId);
        log.info("@# memberId => {}", memberId);

        EvReservationDTO reservation = reservationDAO.findReservationById(reservationId, memberId);

        if (reservation == null) {
            throw new IllegalArgumentException("예약 정보를 찾을 수 없습니다.");
        }

        if (!"충전중".equals(reservation.getStatus())) {
            throw new IllegalArgumentException("충전중 상태의 예약만 완료 처리할 수 있습니다.");
        }

        reservationDAO.upsertChargingSessionForSimulation(reservationId, memberId);

        int updateCount = reservationDAO.completeReservationForSimulation(reservationId, memberId);

        if (updateCount == 0) {
            throw new IllegalArgumentException("충전 완료 처리에 실패했습니다.");
        }

        reservationDAO.completeChargingSessionForSimulation(reservationId, memberId);
        reservationDAO.updateChargerStatus(reservation.getChargerId(), "사용가능");
        evChargerRedisService.setChargerStatus(reservation.getChargerId(), "AVAILABLE");

        return reservationDAO.findReservationById(reservationId, memberId);
    }
    
    @Override
    @Transactional
    public int updateReservationStatusAutomatically() {
        log.info("@# EvReservationServiceImpl.updateReservationStatusAutomatically()");

        /*
         * 예약완료 → 노쇼
         */
        int noShowCount = reservationDAO.updateReservationCompleteToNoShow();
        log.info("@# noShowCount => {}", noShowCount);

        int noShowChargerResetCount = reservationDAO.updateNoShowChargersToAvailable();
        log.info("@# noShowChargerResetCount => {}", noShowChargerResetCount);

        /*
         * 인증완료 → 충전중
         */
        int chargingCount = reservationDAO.updateAuthenticatedToCharging();
        log.info("@# chargingCount => {}", chargingCount);

        /*
         * 충전중 상태가 된 예약에 대해 charging_session 생성
         */
        int sessionInsertCount =
                reservationDAO.insertChargingSessionForChargingReservations();
        log.info("@# sessionInsertCount => {}", sessionInsertCount);

        /*
         * 충전중 → 완료
         */
        int completeCount = reservationDAO.updateChargingToComplete();
        log.info("@# completeCount => {}", completeCount);

        int completedChargerResetCount = reservationDAO.updateCompletedChargersToAvailable();
        log.info("@# completedChargerResetCount => {}", completedChargerResetCount);

        /*
         * 인증완료 상태였지만 이미 종료 시간이 지난 예약 → 완료
         */
        int authenticatedCompleteCount = reservationDAO.updateAuthenticatedToComplete();
        log.info("@# authenticatedCompleteCount => {}", authenticatedCompleteCount);

        /*
         * 충전중 charging_session → 완료 처리
         */
        int sessionCompleteCount =
                reservationDAO.updateChargingSessionToComplete();
        log.info("@# sessionCompleteCount => {}", sessionCompleteCount);

        /*
         * 완료 상태인데 charging_session이 없는 예약 보정 생성
         */
        int completedSessionInsertCount =
                reservationDAO.insertChargingSessionForCompletedReservations();
        log.info("@# completedSessionInsertCount => {}", completedSessionInsertCount);

        int totalCount =
                noShowCount
                + noShowChargerResetCount
                + chargingCount
                + sessionInsertCount
                + completeCount
                + completedChargerResetCount
                + authenticatedCompleteCount
                + sessionCompleteCount
                + completedSessionInsertCount;

        log.info("@# total reservation/session update count => {}", totalCount);

        return totalCount;
    }
    
    /*
     * 충전 내역 목록 조회
     *
     * 현재는 별도 충전 내역 테이블을 만들지 않고
     * 기존 내 예약 목록에서 status가 '완료'인 예약만 필터링한다.
     */
    @Override
    public List<EvReservationDTO> getChargingHistoryList(Long memberId,
                                                         LocalDateTime startDate,
                                                         LocalDateTime endDate) {
        log.info("@# EvReservationServiceImpl.getChargingHistoryList()");
        log.info("@# memberId => {}", memberId);
        log.info("@# startDate => {}", startDate);
        log.info("@# endDate => {}", endDate);

        return reservationDAO.getChargingHistoryList(memberId, startDate, endDate);
    }
    
    
    /*
     * 충전 영수증 이메일 발송
     *
     * 조건:
     * 1. 로그인한 회원 본인의 예약이어야 한다.
     * 2. 충전 상태가 완료여야 한다.
     * 3. DB에 회원 이메일이 있어야 한다.
     */
    @Override
    public void sendReceiptEmail(Long reservationId, Long memberId) {
        log.info("@# EvReservationServiceImpl.sendReceiptEmail()");
        log.info("@# reservationId => {}", reservationId);
        log.info("@# memberId => {}", memberId);

        /*
         * 로그인 회원의 최신 이메일 조회
         *
         * EvUserDetails에 email이 없어도
         * DB에서 memberId 기준으로 이메일을 가져온다.
         */
        String receiverEmail = reservationDAO.findMemberEmailByMemberId(memberId);

        log.info("@# receiverEmail => {}", receiverEmail);

        if (receiverEmail == null || receiverEmail.trim().isEmpty()) {
            throw new IllegalArgumentException("회원 이메일 정보가 없습니다.");
        }

        if (!receiverEmail.contains("@")) {
            throw new IllegalArgumentException("회원 이메일 정보가 올바르지 않습니다.");
        }

        /*
         * reservationId + memberId로 조회하므로
         * 다른 회원의 충전 내역은 조회되지 않는다.
         */
        EvReservationDTO receipt = reservationDAO.findReservationById(reservationId, memberId);

        if (receipt == null) {
            throw new IllegalArgumentException("충전 내역을 찾을 수 없습니다.");
        }

        if (!"완료".equals(receipt.getStatus())) {
            throw new IllegalArgumentException("완료된 충전 내역만 영수증을 발급할 수 있습니다.");
        }

        /*
         * null 방지
         */
        String stationName = receipt.getStationName() == null ? "-" : receipt.getStationName();
        String stationAddress = receipt.getStationAddress() == null ? "-" : receipt.getStationAddress();
        String chargerName = receipt.getChargerName() == null ? "-" : receipt.getChargerName();
        String connectorType = receipt.getConnectorType() == null ? "-" : receipt.getConnectorType();
        String vehicleNickname = receipt.getVehicleNickname() == null ? "" : receipt.getVehicleNickname();
        String modelName = receipt.getModelName() == null ? "-" : receipt.getModelName();

        String vehicleText;

        if (!vehicleNickname.isBlank()) {
            vehicleText = vehicleNickname + " / " + modelName;
        } else {
            vehicleText = modelName;
        }

        String subject = "[EV Charge] 충전 영수증이 발급되었습니다.";

        String body = ""
                + "안녕하세요.\n\n"
                + "EV Charge 충전 이용 영수증입니다.\n\n"
                + "===========================\n"
                + "           충전 영수증\n"
                + "===========================\n"
                + "예약번호: " + receipt.getReservationId() + "\n"
                + "충전상태: " + receipt.getStatus() + "\n"
                + "충전소: " + stationName + "\n"
                + "주소: " + stationAddress + "\n"
                + "충전기: " + chargerName + " · " + connectorType + "\n"
                + "이용 차량: " + vehicleText + "\n"
                + "충전일: " + receipt.getReservationDate() + "\n"
                + "충전시간: " + receipt.getStartTime() + " ~ " + receipt.getEndTime() + "\n"
                + "배터리: " + receipt.getCurrentSoc() + "% → " + receipt.getTargetSoc() + "%\n"
                + "충전량: " + receipt.getRequiredKwh() + "kWh\n"
                + "충전 시간: " + receipt.getEstimatedMinutes() + "분\n"
                + "이용 금액: " + receipt.getEstimatedCost() + "원\n\n"
                + "================================\n\n"
                + "이용해주셔서 감사합니다.\n"
                + "EV Charge";

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(receiverEmail);
        message.setSubject(subject);
        message.setText(body);

        mailSender.send(message);

        log.info("@# receipt email sent");
    }
    
    /*
     * 예약 화면 진입 시 충전기 임시 점유
     */
    @Override
    public boolean holdChargerForReservation(Long chargerId, Long memberId) {
        log.info("@# EvReservationServiceImpl.holdChargerForReservation()");
        log.info("@# chargerId => {}", chargerId);
        log.info("@# memberId => {}", memberId);

        return evChargerRedisService.holdChargerForReservation(chargerId, memberId);
    }

    /*
     * 예약 화면 이탈 또는 예약 완료 시 충전기 임시 점유 해제
     */
    @Override
    public void releaseChargerReservationHold(Long chargerId, Long memberId) {
        log.info("@# EvReservationServiceImpl.releaseChargerReservationHold()");
        log.info("@# chargerId => {}", chargerId);
        log.info("@# memberId => {}", memberId);

        evChargerRedisService.releaseChargerReservationHold(chargerId, memberId);
    }
    
    /*
     * 같은 충전소의 충전기 목록 조회
     */
    @Override
    public List<EvReservationChargerDTO> getReservationChargerList(Long stationId, Long memberId) {
        log.info("@# EvReservationServiceImpl.getReservationChargerList()");
        log.info("@# stationId => {}", stationId);
        log.info("@# memberId => {}", memberId);

        List<EvReservationChargerDTO> chargerList =
                reservationDAO.findReservationChargerListByStationId(stationId);

        for (EvReservationChargerDTO charger : chargerList) {
            boolean statusAvailable =
                    isReservableStationAndCharger(charger);

            /*
             * 시간 구간 기반 선점으로 변경되었기 때문에
             * 단순 충전소 진입 목록에서는 Redis 선점 여부를 표시하지 않는다.
             * 선택 날짜/시간이 정해진 뒤 getChargerStatus()에서 다시 판단한다.
             */
            boolean selectedByOther = false;

            boolean selectable = statusAvailable;

            charger.setSelectedByOther(selectedByOther);
            charger.setSelectable(selectable);

            log.info("@# chargerId => {}", charger.getChargerId());
            log.info("@# chargerStatus => {}", charger.getChargerStatus());
            log.info("@# selectedByOther => {}", selectedByOther);
            log.info("@# selectable => {}", selectable);
        }

        return chargerList;
    }

    /*
     * 예약 폼에서 선택 충전기 변경 시 Redis 임시 점유 변경
     *
     * 처리 순서:
     * 1. 새 충전기가 사용가능인지 확인
     * 2. 새 충전기 lock 시도
     * 3. 새 lock 성공 시 기존 lock 해제
     */
    @Override
    public boolean changeChargerReservationHold(Long beforeChargerId,
                                                Long nextChargerId,
                                                Long memberId) {
        log.info("@# EvReservationServiceImpl.changeChargerReservationHold()");
        log.info("@# beforeChargerId => {}", beforeChargerId);
        log.info("@# nextChargerId => {}", nextChargerId);
        log.info("@# memberId => {}", memberId);

        if (beforeChargerId.equals(nextChargerId)) {
            return true;
        }

        EvReservationChargerDTO nextCharger =
                reservationDAO.findReservationChargerById(nextChargerId);

        if (nextCharger == null) {
            throw new IllegalArgumentException("존재하지 않는 충전기입니다.");
        }

        if (!isReservableStationAndCharger(nextCharger)) {
            throw new IllegalArgumentException(buildNotReservableMessage(nextCharger));
        }

        boolean holdSuccess =
                evChargerRedisService.holdChargerForReservation(nextChargerId, memberId);

        if (!holdSuccess) {
            return false;
        }

        evChargerRedisService.releaseChargerReservationHold(beforeChargerId, memberId);

        return true;
    }
    
    @Override
    public boolean changeReservationHold(Long oldChargerId, Long newChargerId, Long memberId) {
        log.info("@# EvReservationServiceImpl.changeReservationHold()");
        log.info("@# oldChargerId => {}", oldChargerId);
        log.info("@# newChargerId => {}", newChargerId);
        log.info("@# memberId => {}", memberId);

        EvReservationChargerDTO nextCharger = reservationDAO.findReservationChargerById(newChargerId);

        if (!isReservableStationAndCharger(nextCharger)) {
            throw new IllegalArgumentException(buildNotReservableMessage(nextCharger));
        }

        return evChargerRedisService.changeReservationHold(
                oldChargerId,
                newChargerId,
                memberId
        );
    }

    /*
     * 메인페이지 다음 예약 1건 조회
     */
    @Override
    public EvReservationDTO getNextReservation(Long memberId) {
        log.info("@# EvReservationServiceImpl.getNextReservation()");
        log.info("@# memberId => {}", memberId);

        return reservationDAO.findNextReservation(memberId);
    }

    /*
     * 메인페이지 이번 달 충전 비용 합계 조회
     */
    @Override
    public Integer getThisMonthChargingCost(Long memberId) {
        log.info("@# EvReservationServiceImpl.getThisMonthChargingCost()");
        log.info("@# memberId => {}", memberId);

        Integer cost = reservationDAO.findThisMonthChargingCost(memberId);

        if (cost == null) {
            return 0;
        }

        return cost;
    }
    
    @Override
    public List<EvChargerDTO> getChargerStatus(Long stationId,
                                               String reservationDate,
                                               String startTime,
                                               int estimatedMinutes,
                                               Long memberId) {
        log.info("@# EvReservationServiceImpl.getChargerStatus()");
        log.info("@# stationId => {}", stationId);
        log.info("@# reservationDate => {}", reservationDate);
        log.info("@# startTime => {}", startTime);
        log.info("@# estimatedMinutes => {}", estimatedMinutes);
        log.info("@# memberId => {}", memberId);

        LocalDateTime startDateTime = LocalDateTime.parse(reservationDate + "T" + startTime);
        LocalDateTime endDateTime = startDateTime.plusMinutes(estimatedMinutes);
        LocalDateTime holdEndDateTime = endDateTime.plusMinutes(RESERVATION_SLOT_BUFFER_MINUTES);

        /*
         * 1. DB 기준 충전기 상태 조회
         *
         * Mapper에서 처리하는 상태:
         * - 해당 시간대 충전중: 사용중
         * - 해당 시간대 예약완료/인증완료: 예약중
         * - 그 외: 사용가능
         */
        List<EvChargerDTO> chargerList =
                reservationDAO.getChargerStatus(stationId, startDateTime, holdEndDateTime);

        /*
         * 2. Redis 기준 다른 사용자 임시 선점 상태 반영
         *
         * 기존 JSP의 selectedByOther 흐름을 Ajax 조회에서도 유지하기 위한 처리다.
         * 즉, 다른 사용자가 예약 폼에서 선택 중인 충전기는
         * 새로고침 없이도 "선택중"으로 내려보낸다.
         */
        for (EvChargerDTO charger : chargerList) {
            boolean selectedByOther =
                    evChargerRedisService.isReservationTimeSlotSelectedByOther(
                            charger.getChargerId(),
                            memberId,
                            startDateTime,
                            holdEndDateTime
                    );

            /*
             * DB 기준으로 이미 사용중/예약중인 충전기는 DB 상태를 우선한다.
             * Redis 선점은 아직 예약 등록 전의 임시 상태이므로
             * 실제 예약/충전 상태보다 우선하면 안 된다.
             */
            boolean unavailableByDbStatus = !"사용가능".equals(charger.getStatus());

            if (selectedByOther && !unavailableByDbStatus) {
                charger.setStatus("선점중");
                charger.setReserved(true);
                charger.setSelectedByOther(true);
            }

            log.info("@# chargerId => {}", charger.getChargerId());
            log.info("@# chargerStatus => {}", charger.getStatus());
            log.info("@# selectedByOther => {}", selectedByOther);
            log.info("@# reserved => {}", charger.isReserved());
        }

        return chargerList;
    }



    @Override
    public boolean holdReservationTimeSlot(Long chargerId,
                                           Long memberId,
                                           String reservationDate,
                                           String startTime,
                                           int estimatedMinutes) {
        log.info("@# EvReservationServiceImpl.holdReservationTimeSlot()");
        log.info("@# chargerId => {}", chargerId);
        log.info("@# memberId => {}", memberId);
        log.info("@# reservationDate => {}", reservationDate);
        log.info("@# startTime => {}", startTime);
        log.info("@# estimatedMinutes => {}", estimatedMinutes);

        if (chargerId == null || memberId == null || reservationDate == null || startTime == null || estimatedMinutes <= 0) {
            throw new IllegalArgumentException("예약 선점에 필요한 정보가 부족합니다.");
        }

        EvReservationChargerDTO charger = reservationDAO.findReservationChargerById(chargerId);

        if (!isReservableStationAndCharger(charger)) {
            throw new IllegalArgumentException(buildNotReservableMessage(charger));
        }

        LocalDateTime startDateTime = LocalDateTime.parse(reservationDate + "T" + startTime);
        LocalDateTime endDateTime = startDateTime.plusMinutes(estimatedMinutes);
        LocalDateTime holdEndDateTime = endDateTime.plusMinutes(RESERVATION_SLOT_BUFFER_MINUTES);

        if (startDateTime.isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("현재 시간보다 이전 시간으로 예약할 수 없습니다.");
        }

        int overlapCount = reservationDAO.countReservationOverlap(chargerId, startDateTime, holdEndDateTime);

        if (overlapCount > 0) {
            log.info("@# holdReservationTimeSlot overlapCount => {}", overlapCount);
            return false;
        }

        return evChargerRedisService.holdReservationTimeSlot(
                chargerId,
                memberId,
                startDateTime,
                holdEndDateTime
        );
    }

    @Override
    public void releaseReservationTimeSlotHold(Long chargerId,
                                               Long memberId,
                                               String reservationDate,
                                               String startTime,
                                               int estimatedMinutes) {
        log.info("@# EvReservationServiceImpl.releaseReservationTimeSlotHold()");
        log.info("@# chargerId => {}", chargerId);
        log.info("@# memberId => {}", memberId);
        log.info("@# reservationDate => {}", reservationDate);
        log.info("@# startTime => {}", startTime);
        log.info("@# estimatedMinutes => {}", estimatedMinutes);

        if (chargerId == null || memberId == null || reservationDate == null || startTime == null || estimatedMinutes <= 0) {
            return;
        }

        LocalDateTime startDateTime = LocalDateTime.parse(reservationDate + "T" + startTime);
        LocalDateTime endDateTime = startDateTime.plusMinutes(estimatedMinutes);
        LocalDateTime holdEndDateTime = endDateTime.plusMinutes(RESERVATION_SLOT_BUFFER_MINUTES);

        evChargerRedisService.releaseReservationTimeSlotHold(
                chargerId,
                memberId,
                startDateTime,
                holdEndDateTime
        );
    }

}
