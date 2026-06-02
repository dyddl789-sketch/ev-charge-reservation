package com.ev.service.user;

import java.util.List;
import java.util.stream.Collectors;


import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
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
    private final JavaMailSender mailSender;
    
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

        return reservationDAO.getMyReservationList(memberId);
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

        /*
         * 예약완료 → 노쇼
         */
        int noShowCount = reservationDAO.updateReservationCompleteToNoShow();
        log.info("@# noShowCount => {}", noShowCount);

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
                + chargingCount
                + sessionInsertCount
                + completeCount
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
    public List<EvReservationDTO> getChargingHistoryList(Long memberId) {
        log.info("@# EvReservationServiceImpl.getChargingHistoryList()");
        log.info("@# memberId => {}", memberId);

        /*
         * 기존 내 예약 목록 조회 메서드를 재사용한다.
         *
         * 메서드명이 다르면 네 프로젝트에서
         * 내 예약 목록 조회에 쓰고 있는 DAO 메서드명으로 변경하면 된다.
         */
        List<EvReservationDTO> reservationList = reservationDAO.getMyReservationList(memberId);

        return reservationList.stream()
                .filter(reservation -> "완료".equals(reservation.getStatus()))
                .collect(Collectors.toList());
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
                    "사용가능".equals(charger.getChargerStatus());

            boolean selectedByOther =
                    evChargerRedisService.isReservationSelectedByOther(
                            charger.getChargerId(),
                            memberId
                    );

            boolean selectable =
                    statusAvailable && !selectedByOther;

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

        if (!"사용가능".equals(nextCharger.getChargerStatus())) {
            throw new IllegalArgumentException("현재 선택할 수 없는 충전기입니다.");
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

        return evChargerRedisService.changeReservationHold(
                oldChargerId,
                newChargerId,
                memberId
        );
    }
}
