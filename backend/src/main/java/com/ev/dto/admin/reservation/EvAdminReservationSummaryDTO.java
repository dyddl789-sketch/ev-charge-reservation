package com.ev.dto.admin.reservation;

import lombok.Data;

/*
 * 관리자 예약 현황 상단 요약 DTO
 */
@Data
public class EvAdminReservationSummaryDTO {

    private Long totalCount;
    private Long reservedCount;
    private Long verifiedCount;
    private Long chargingCount;
    private Long completedCount;
    private Long canceledCount;
    private Long noShowCount;
}