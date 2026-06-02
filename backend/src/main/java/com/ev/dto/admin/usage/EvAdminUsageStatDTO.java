package com.ev.dto.admin.usage;

import java.util.List;

import lombok.Data;

/*
 * 이용 통계 화면 전체 DTO
 */
@Data
public class EvAdminUsageStatDTO {

    private EvAdminUsageSummaryDTO summary;

    private List<EvAdminUsageDailyDTO> dailyList;
    private List<EvAdminUsageHourlyDTO> hourlyList;
    private List<EvAdminUsageTypeDTO> typeList;
    private List<EvAdminUsageStationRankDTO> stationRankList;
}