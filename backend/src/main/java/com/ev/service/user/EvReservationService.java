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
}