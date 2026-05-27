package com.ev.service.user;

import java.util.List;
import java.util.UUID;

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
     * 1. 차량 정보 조회
     * 2. 충전기 정보 조회
     * 3. 예약 시간 검증
     * 4. 예상 충전량 / 예상 시간 / 예상 금액 계산
     * 5. 인증 코드 생성
     * 6. 예약 insert
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

        if (reservationDTO.getReservationDate() == null) {
            throw new IllegalArgumentException("예약 날짜를 선택하세요.");
        }

        if (reservationDTO.getStartTime() == null || reservationDTO.getEndTime() == null) {
            throw new IllegalArgumentException("예약 시간을 선택하세요.");
        }

        if (!reservationDTO.getStartTime().isBefore(reservationDTO.getEndTime())) {
            throw new IllegalArgumentException("종료 시간은 시작 시간보다 늦어야 합니다.");
        }

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
         *
         * memberId도 같이 조건에 넣는 이유:
         * - 다른 회원의 차량으로 예약하는 것을 막기 위해서
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
                reservationDTO.getReservationDate(),
                reservationDTO.getStartTime(),
                reservationDTO.getEndTime()
        );

        if (overlapCount > 0) {
            throw new IllegalArgumentException("이미 해당 시간에 예약이 존재합니다.");
        }

        /*
         * 5. 예상 충전량 계산
         *
         * 계산식:
         * 배터리 용량 * ((목표 SOC - 현재 SOC) / 100)
         *
         * 예:
         * 77.4kWh 차량
         * 현재 30%, 목표 80%
         * 77.4 * 0.5 = 38.7kWh
         */
        double batteryCapacity = vehicle.getBatteryCapacityKwh();

        double socDiff = reservationDTO.getTargetSoc() - reservationDTO.getCurrentSoc();

        double requiredKwh = batteryCapacity * (socDiff / 100.0);

        /*
         * 소수점 둘째 자리까지 반올림
         */
        requiredKwh = Math.round(requiredKwh * 100.0) / 100.0;


        /*
         * 6. 예상 충전 시간 계산
         *
         * 계산식:
         * 예상 충전량 / 충전 속도 * 60
         */
        double estimatedMinutesDouble = requiredKwh / charger.getChargingSpeedKw() * 60;

        int estimatedMinutes = (int) Math.ceil(estimatedMinutesDouble);


        /*
         * 7. 예상 금액 계산
         *
         * 계산식:
         * 예상 충전량 * kWh당 가격
         */
        double estimatedCost = requiredKwh * charger.getPricePerKwh();

        /*
         * 원 단위 반올림
         */
        estimatedCost = Math.round(estimatedCost);


        /*
         * 8. 예약 기본값 세팅
         */
        reservationDTO.setRequiredKwh(requiredKwh);
        reservationDTO.setEstimatedMinutes(estimatedMinutes);
        reservationDTO.setEstimatedCost(estimatedCost);
        reservationDTO.setStatus("예약완료");

        /*
         * 인증 코드 생성
         *
         * 예:
         * A1B2C3
         */
        String authCode = UUID.randomUUID()
                .toString()
                .replace("-", "")
                .substring(0, 6)
                .toUpperCase();

        reservationDTO.setAuthCode(authCode);

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
}