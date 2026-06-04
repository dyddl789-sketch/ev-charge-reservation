package com.ev.dto.admin.sales;

import lombok.Data;

/*
 * 충전 타입별 매출 현황 DTO
 */
@Data
public class EvAdminSalesTypeDTO {

    private String chargerType;
    private Long salesAmount;
    private Double salesRate;
}