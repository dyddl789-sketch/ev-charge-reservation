package com.ev.dao.user;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.ev.dto.reservation.EvReservationChargerDTO;
import com.ev.dto.reservation.EvReservationDTO;
import com.ev.dto.vehicle.EvVehicleDTO;

/*
 * 사용자 예약 관련 DAO
 *
 * Mapper XML:
 * src/main/resources/mapper/user/ReservationMapper.xml
 */

@Mapper
public interface EvReservationDAO {

    /*
     * 예약 폼에서 보여줄 충전기 + 충전소 정보 조회
     */
    EvReservationChargerDTO findReservationChargerById(@Param("chargerId") Long chargerId);

    /*
     * 로그인한 회원의 차량 목록 조회
     */
    List<EvVehicleDTO> findVehicleListByMemberId(@Param("memberId") Long memberId);

    /*
     * 예약 계산용 차량 정보 조회
     *
     * 차량 배터리 용량을 가져오기 위해 사용한다.
     */
    EvVehicleDTO findVehicleById(@Param("vehicleId") Long vehicleId,
                               @Param("memberId") Long memberId);

    /*
     * 예약 시간 중복 체크
     *
     * 같은 충전기에 같은 날짜, 겹치는 시간이 있으면 예약 불가.
     */
    int countReservationOverlap(@Param("chargerId") Long chargerId,
                                @Param("reservationDate") LocalDate reservationDate,
                                @Param("startTime") LocalTime startTime,
                                @Param("endTime") LocalTime endTime);

    /*
     * 예약 등록
     */
    int insertReservation(EvReservationDTO reservationDTO);

    /*
     * 예약 완료 화면에서 예약 정보 조회
     */
    EvReservationDTO findReservationById(@Param("reservationId") Long reservationId,
                                       @Param("memberId") Long memberId);
}