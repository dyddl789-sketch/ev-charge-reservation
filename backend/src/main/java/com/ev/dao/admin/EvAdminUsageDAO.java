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

    EvAdminUsageSummaryDTO findUsageSummary(@Param("startDate") String startDate,
                                            @Param("endDate") String endDate,
                                            @Param("region") String region,
                                            @Param("stationId") Long stationId,
                                            @Param("chargerType") String chargerType);

    List<EvAdminUsageDailyDTO> findDailyUsageList(@Param("startDate") String startDate,
                                                  @Param("endDate") String endDate,
                                                  @Param("region") String region,
                                                  @Param("stationId") Long stationId,
                                                  @Param("chargerType") String chargerType);

    List<EvAdminUsageHourlyDTO> findHourlyUsageList(@Param("startDate") String startDate,
                                                    @Param("endDate") String endDate,
                                                    @Param("region") String region,
                                                    @Param("stationId") Long stationId,
                                                    @Param("chargerType") String chargerType);

    List<EvAdminUsageTypeDTO> findUsageTypeList(@Param("startDate") String startDate,
                                                @Param("endDate") String endDate,
                                                @Param("region") String region,
                                                @Param("stationId") Long stationId,
                                                @Param("chargerType") String chargerType);

    List<EvAdminUsageStationRankDTO> findStationUsageRankList(@Param("startDate") String startDate,
                                                              @Param("endDate") String endDate,
                                                              @Param("region") String region,
                                                              @Param("stationId") Long stationId,
                                                              @Param("chargerType") String chargerType);
}
