package com.ev.dto.admin.usage;

import lombok.Data;

/*
 * 충전소별 이용 순위 DTO
 */
@Data
public class EvAdminUsageStationRankDTO {

    private Integer rankNo;
    private Long stationId;
    private String stationName;
    private Long usageCount;
    private Long avgMinutes;
    private Double operationRate;
}