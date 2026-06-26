package com.ev.dto.admin.usage;

import lombok.Data;

/*
 * 일별/월별 이용 현황 DTO
 */
@Data
public class EvAdminUsageDailyDTO {

    private String usageDate;
    private Long usageCount;
    private Double totalKwh;
    private Long avgChargingMinutes;
}
