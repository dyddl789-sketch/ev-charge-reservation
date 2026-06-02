package com.ev.dto.admin.usage;

import lombok.Data;

/*
 * 관리자 이용 통계 요약 DTO
 */
@Data
public class EvAdminUsageSummaryDTO {

    private Long totalUsageCount;
    private Long todayUsageCount;
    private Long avgChargingMinutes;
    private Double totalKwh;

    private Long diffUsageCount;
}