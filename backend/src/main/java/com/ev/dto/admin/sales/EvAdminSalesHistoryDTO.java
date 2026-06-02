package com.ev.dto.admin.sales;

import lombok.Data;

/*
 * 최근 매출 내역 DTO
 */
@Data
public class EvAdminSalesHistoryDTO {

    private Long sessionId;
    private String paymentDate;
    private String memberName;
    private String stationName;
    private String chargerName;
    private Double actualKwh;
    private Long actualCost;
    private String status;
}