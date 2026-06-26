package com.ev.dto.admin.usage;

import lombok.Data;

/*
 * 시간대별 이용 현황 DTO
 */
@Data
public class EvAdminUsageHourlyDTO {

    private String usageHour;
    private Long usageCount;
    private Double totalKwh;
    private Long avgChargingMinutes;
}
