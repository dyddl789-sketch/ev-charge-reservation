package com.ev.dto.admin.sales;

import lombok.Data;

/*
 * 일별/월별 매출 현황 DTO
 */
@Data
public class EvAdminSalesDailyDTO {

    private String salesDate;
    private Long salesAmount;
    private Long paymentCount;
    private Long avgPaymentAmount;
}
