package com.ev.service.user;

import java.util.List;

import com.ev.dto.reservation.EvReservationChargerDTO;
import com.ev.dto.reservation.EvReservationDTO;
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
    List<EvReservationDTO> getMyReservationList(Long memberId);

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
}
