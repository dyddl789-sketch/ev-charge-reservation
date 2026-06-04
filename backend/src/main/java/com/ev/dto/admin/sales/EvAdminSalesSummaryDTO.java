package com.ev.dto.admin.sales;

import lombok.Data;

/*
 * 관리자 매출 통계 요약 DTO
 */
@Data
public class EvAdminSalesSummaryDTO {

    private Long totalSalesAmount;
    private Long todaySalesAmount;
    private Long avgPaymentAmount;
    private Double totalKwh;

    private Long diffSalesAmount;
}