package com.ev.dto.admin.sales;

import lombok.Data;

/*
 * 충전소별 매출 순위 DTO
 */
@Data
public class EvAdminSalesStationRankDTO {

    private Integer rankNo;
    private Long stationId;
    private String stationName;
    private Long salesAmount;
    private Long paymentCount;
    private Double totalKwh;
    private Long avgPaymentAmount;

    // 선택 조회기간 내 전체 매출 대비 비율
    private Double salesRate;
}