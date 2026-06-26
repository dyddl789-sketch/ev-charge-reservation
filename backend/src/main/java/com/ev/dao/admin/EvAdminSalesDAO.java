package com.ev.dao.admin;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.ev.dto.admin.sales.EvAdminSalesDailyDTO;
import com.ev.dto.admin.sales.EvAdminSalesHistoryDTO;
import com.ev.dto.admin.sales.EvAdminSalesHourlyDTO;
import com.ev.dto.admin.sales.EvAdminSalesStationRankDTO;
import com.ev.dto.admin.sales.EvAdminSalesSummaryDTO;
import com.ev.dto.admin.sales.EvAdminSalesTypeDTO;

@Mapper
public interface EvAdminSalesDAO {

    EvAdminSalesSummaryDTO findSalesSummary(@Param("startDate") String startDate,
                                            @Param("endDate") String endDate,
                                            @Param("region") String region,
                                            @Param("stationId") Long stationId,
                                            @Param("chargerType") String chargerType);

    List<EvAdminSalesDailyDTO> findDailySalesList(@Param("startDate") String startDate,
                                                  @Param("endDate") String endDate,
                                                  @Param("region") String region,
                                                  @Param("stationId") Long stationId,
                                                  @Param("chargerType") String chargerType);

    List<EvAdminSalesHourlyDTO> findHourlySalesList(@Param("startDate") String startDate,
                                                    @Param("endDate") String endDate,
                                                    @Param("region") String region,
                                                    @Param("stationId") Long stationId,
                                                    @Param("chargerType") String chargerType);

    List<EvAdminSalesTypeDTO> findSalesTypeList(@Param("startDate") String startDate,
                                                @Param("endDate") String endDate,
                                                @Param("region") String region,
                                                @Param("stationId") Long stationId,
                                                @Param("chargerType") String chargerType);

    List<EvAdminSalesStationRankDTO> findStationSalesRankList(@Param("startDate") String startDate,
                                                              @Param("endDate") String endDate,
                                                              @Param("region") String region,
                                                              @Param("stationId") Long stationId,
                                                              @Param("chargerType") String chargerType);

    List<EvAdminSalesHistoryDTO> findSalesHistoryList(@Param("startDate") String startDate,
                                                      @Param("endDate") String endDate,
                                                      @Param("region") String region,
                                                      @Param("stationId") Long stationId,
                                                      @Param("chargerType") String chargerType);
}
