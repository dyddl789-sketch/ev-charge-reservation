package com.ev.dao.admin;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.ev.dto.admin.reservation.EvAdminReservationIssueDTO;
import com.ev.dto.admin.reservation.EvAdminReservationListDTO;
import com.ev.dto.admin.reservation.EvAdminReservationSearchDTO;
import com.ev.dto.admin.reservation.EvAdminReservationSummaryDTO;

@Mapper
public interface EvAdminReservationDAO {

    // 예약 현황 요약 조회
    EvAdminReservationSummaryDTO findReservationSummary(EvAdminReservationSearchDTO searchDTO);

    // 오늘 예약 이슈 조회
    EvAdminReservationIssueDTO findTodayReservationIssue();

    // 예약 목록 수 조회
    Long countReservationList(EvAdminReservationSearchDTO searchDTO);

    // 예약 목록 조회
    List<EvAdminReservationListDTO> findReservationList(EvAdminReservationSearchDTO searchDTO);

    // 예약 취소 처리
    void cancelReservation(@Param("reservationId") Long reservationId);

    // 노쇼 처리
    void noShowReservation(@Param("reservationId") Long reservationId);

    // 충전 시작 처리
    void startCharging(@Param("reservationId") Long reservationId);
}