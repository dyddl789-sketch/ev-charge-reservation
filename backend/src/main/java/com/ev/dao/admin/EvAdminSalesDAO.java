package com.ev.dao.admin;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.ev.dto.admin.sales.EvAdminSalesDailyDTO;
import com.ev.dto.admin.sales.EvAdminSalesHistoryDTO;
import com.ev.dto.admin.sales.EvAdminSalesStationRankDTO;
import com.ev.dto.admin.sales.EvAdminSalesSummaryDTO;
import com.ev.dto.admin.sales.EvAdminSalesTypeDTO;

@Mapper
public interface EvAdminSalesDAO {

    // 매출 통계 요약 조회
    EvAdminSalesSummaryDTO findSalesSummary(
            @Param("startDate") String startDate,
            @Param("endDate") String endDate
    );

    // 일별 매출 현황 조회
    List<EvAdminSalesDailyDTO> findDailySalesList(
            @Param("startDate") String startDate,
            @Param("endDate") String endDate
    );

    // 충전 타입별 매출 현황 조회
    List<EvAdminSalesTypeDTO> findSalesTypeList(
            @Param("startDate") String startDate,
            @Param("endDate") String endDate
    );

    // 충전소별 매출 순위 조회
    List<EvAdminSalesStationRankDTO> findStationSalesRankList(
            @Param("startDate") String startDate,
            @Param("endDate") String endDate
    );

    // 최근 매출 내역 조회
    List<EvAdminSalesHistoryDTO> findSalesHistoryList(
            @Param("startDate") String startDate,
            @Param("endDate") String endDate
    );
}