package com.ev.dto.admin.usage;

import lombok.Data;

/*
 * 충전 타입별 이용 현황 DTO
 */
@Data
public class EvAdminUsageTypeDTO {

    private String chargerType;
    private Long usageCount;
    private Double usageRate;
    private Long avgMinutes;
}