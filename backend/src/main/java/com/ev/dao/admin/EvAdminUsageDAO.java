package com.ev.dao.admin;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.ev.dto.admin.usage.EvAdminUsageDailyDTO;
import com.ev.dto.admin.usage.EvAdminUsageHourlyDTO;
import com.ev.dto.admin.usage.EvAdminUsageStationRankDTO;
import com.ev.dto.admin.usage.EvAdminUsageSummaryDTO;
import com.ev.dto.admin.usage.EvAdminUsageTypeDTO;

@Mapper
public interface EvAdminUsageDAO {

    // 이용 통계 요약 조회
    EvAdminUsageSummaryDTO findUsageSummary(
            @Param("startDate") String startDate,
            @Param("endDate") String endDate
    );

    // 일별 이용 현황 조회
    List<EvAdminUsageDailyDTO> findDailyUsageList(
            @Param("startDate") String startDate,
            @Param("endDate") String endDate
    );

    // 시간대별 이용 현황 조회
    List<EvAdminUsageHourlyDTO> findHourlyUsageList(
            @Param("startDate") String startDate,
            @Param("endDate") String endDate
    );

    // 충전 타입별 이용 현황 조회
    List<EvAdminUsageTypeDTO> findUsageTypeList(
            @Param("startDate") String startDate,
            @Param("endDate") String endDate
    );

    // 충전소별 이용 순위 조회
    List<EvAdminUsageStationRankDTO> findStationUsageRankList(
            @Param("startDate") String startDate,
            @Param("endDate") String endDate
    );
}