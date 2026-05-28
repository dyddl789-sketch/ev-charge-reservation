package com.ev.dao.user;

import java.time.LocalDateTime;
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
 * src/main/resources/mybatis/mappers/user/EvReservationMapper.xml
 */
@Mapper
public interface EvReservationDAO {

    /*
     * 예약 폼에서 보여줄 충전기 + 충전소 정보 조회
     */
    EvReservationChargerDTO findReservationChargerById(
            @Param("chargerId") Long chargerId
    );

    /*
     * 로그인한 회원의 차량 목록 조회
     */
    List<EvVehicleDTO> findVehicleListByMemberId(
            @Param("memberId") Long memberId
    );

    /*
     * 예약 계산용 차량 정보 조회
     *
     * memberId를 같이 받는 이유:
     * - 다른 회원의 차량 ID를 URL이나 form 조작으로 넘기는 것을 막기 위해서
     */
    EvVehicleDTO findVehicleById(
            @Param("vehicleId") Long vehicleId,
            @Param("memberId") Long memberId
    );

    /*
     * 예약 시간 중복 체크
     */
    int countReservationOverlap(
            @Param("chargerId") Long chargerId,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime
    );

    /*
     * 예약 등록
     */
    int insertReservation(EvReservationDTO reservationDTO);

    /*
     * 예약 완료 화면에서 예약 정보 조회
     */
    EvReservationDTO findReservationById(
            @Param("reservationId") Long reservationId,
            @Param("memberId") Long memberId
    );

    /*
     * 내 예약 목록 조회
     */
    List<EvReservationDTO> findReservationListByMemberId(
            @Param("memberId") Long memberId
    );

    /*
     * 예약 취소
     */
    int cancelReservation(@Param("reservationId") Long reservationId,
                          @Param("memberId") Long memberId);

    /*
     * 인증 가능한 예약 조회
     *
     * 조건:
     * - 본인 예약
     * - 예약완료 상태
     * - 예약 시작 10분 전부터 종료 시간 사이
     */
    EvReservationDTO findVerifiableReservation(@Param("reservationId") Long reservationId,
                                               @Param("memberId") Long memberId);

    /*
     * 예약 상태를 인증완료로 변경
     */
    int updateReservationStatusToVerified(@Param("reservationId") Long reservationId,
                                          @Param("memberId") Long memberId);
    
    /*
     * 예약완료 → 노쇼
     */
    int updateReservationCompleteToNoShow();

    /*
     * 인증완료 → 완료
     */
    int updateAuthenticatedToComplete();

    /*
     * 인증완료 → 충전중
     */
    int updateAuthenticatedToCharging();

    /*
     * 충전중 → 완료
     */
    int updateChargingToComplete();
}
