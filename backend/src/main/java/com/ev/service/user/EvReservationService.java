package com.ev.service.user;

import java.time.LocalDateTime;
import java.util.List;

import com.ev.dto.reservation.EvReservationChargerDTO;
import com.ev.dto.reservation.EvReservationDTO;
import com.ev.dto.station.EvChargerDTO;
import com.ev.dto.vehicle.EvVehicleDTO;

/*
 * 사용자 예약 관련 Service
 */
public interface EvReservationService {

    /*
     * 예약 폼 충전기 정보 조회
     */
    EvReservationChargerDTO getReservationCharger(Long chargerId);

    /*
     * 회원 차량 목록 조회
     */
    List<EvVehicleDTO> getVehicleList(Long memberId);

    /*
     * 예약 등록
     */
    Long createReservation(EvReservationDTO reservationDTO);

    /*
     * 예약 완료 정보 조회
     */
    EvReservationDTO getReservationComplete(Long reservationId, Long memberId);

    /*
     * 내 예약 목록 조회
     */
    List<EvReservationDTO> getMyReservationList(Long memberId, LocalDateTime startDate, LocalDateTime endDate);

    /*
     * 예약 취소
     */
    void cancelReservation(Long reservationId, Long memberId);

    /*
     * 예약 현장 인증코드 발급
     *
     * 조건:
     * - 로그인한 회원 본인의 예약
     * - 예약 상태가 '예약완료'
     * - 예약 시작 10분 전부터 예약 종료 시간 사이
     */
    String issueAuthCode(Long reservationId, Long memberId);

    /*
     * 예약 현장 인증
     *
     * 조건:
     * - 로그인한 회원 본인의 예약
     * - 예약 상태가 '예약완료'
     * - 예약 시작 10분 전부터 예약 종료 시간 사이
     * - Redis에 저장된 인증코드와 입력 코드 일치
     */
    void verifyReservation(Long reservationId, Long memberId, String authCode);
    
    /*
     * 예약 상태 자동 변경
     */
    int updateReservationStatusAutomatically();
    
    /*
     * 충전 내역 목록 조회
     *
     * 로그인한 회원의 예약 중
     * 충전이 완료된 예약만 조회한다.
     */
    List<EvReservationDTO> getChargingHistoryList(Long memberId, LocalDateTime startDate, LocalDateTime endDate);
    
    /*
     * 충전 영수증 이메일 발송
     *
     * 로그인한 회원 본인의 완료된 충전 내역만 발송한다.
     */
    /*
     * 충전 영수증 이메일 발송
     *
     * 로그인한 회원 본인의 완료된 충전 내역만 발송한다.
     */
    void sendReceiptEmail(Long reservationId, Long memberId);
    
    /*
     * 예약 화면 진입 시 충전기 임시 점유
     *
     * 다른 사용자가 같은 충전기 예약 정보를 입력 중이면 false 반환
     */
    boolean holdChargerForReservation(Long chargerId, Long memberId);

    /*
     * 예약 화면 이탈 또는 예약 완료 시 충전기 임시 점유 해제
     */
    void releaseChargerReservationHold(Long chargerId, Long memberId);
    
    /*
     * 같은 충전소의 충전기 목록 조회
     */
    List<EvReservationChargerDTO> getReservationChargerList(Long stationId, Long memberId);

    /*
     * 예약 폼에서 선택 충전기 변경 시 Redis 임시 점유 변경
     */
    boolean changeChargerReservationHold(Long beforeChargerId,
                                         Long nextChargerId,
                                         Long memberId);
    
    boolean changeReservationHold(Long oldChargerId, Long newChargerId, Long memberId);

    /*
     * 메인페이지 다음 예약 1건 조회
     */
    EvReservationDTO getNextReservation(Long memberId);

    /*
     * 메인페이지 이번 달 충전 비용 합계 조회
     */
    Integer getThisMonthChargingCost(Long memberId);
    
    /*
     * 선택한 예약 시간 기준 충전기 상태 조회
     */
    List<EvChargerDTO> getChargerStatus(Long stationId,
                                        String reservationDate,
                                        String startTime,
                                        int estimatedMinutes);
}
