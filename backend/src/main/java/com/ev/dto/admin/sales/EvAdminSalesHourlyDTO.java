package com.ev.dto.admin.sales;

import lombok.Data;

/*
 * 시간대별 매출 현황 DTO
 */
@Data
public class EvAdminSalesHourlyDTO {

    private String salesHour;
    private Long salesAmount;

    private Long paymentCount;

    private Long avgPaymentAmount;

    private java.math.BigDecimal totalKwh;
}
