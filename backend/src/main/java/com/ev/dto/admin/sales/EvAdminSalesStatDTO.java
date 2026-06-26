package com.ev.dto.admin.sales;

import java.util.List;

import lombok.Data;

/*
 * 매출 통계 화면 전체 DTO
 */
@Data
public class EvAdminSalesStatDTO {

    private EvAdminSalesSummaryDTO summary;

    private List<EvAdminSalesDailyDTO> dailyList;
    private List<EvAdminSalesHourlyDTO> hourlyList;
    private List<EvAdminSalesTypeDTO> typeList;
    private List<EvAdminSalesStationRankDTO> stationRankList;
    private List<EvAdminSalesHistoryDTO> historyList;
}