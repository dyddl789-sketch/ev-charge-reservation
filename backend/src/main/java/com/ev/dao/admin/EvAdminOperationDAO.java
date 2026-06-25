package com.ev.dao.admin;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.ev.dto.admin.simulation.EvAdminSimulationChargerDTO;

/*
 * MIS 대시보드 운영 시뮬레이션 DAO
 */
@Mapper
public interface EvAdminOperationDAO {

    EvAdminSimulationChargerDTO findRandomAvailableBusanCharger();

    int updateChargerStatus(@Param("chargerId") Long chargerId,
                            @Param("status") String status);

    int countOpenFaultByCharger(@Param("chargerId") Long chargerId);

    int insertSystemFaultFromCharger(@Param("chargerId") Long chargerId,
                                     @Param("faultType") String faultType,
                                     @Param("title") String title,
                                     @Param("description") String description,
                                     @Param("severity") String severity,
                                     @Param("sourceType") String sourceType);

    int resetBrokenChargers();

    int countBrokenChargers();

    int insertPublicApiSyncLog(@Param("apiName") String apiName,
                               @Param("syncType") String syncType,
                               @Param("status") String status,
                               @Param("requestCount") int requestCount,
                               @Param("successCount") int successCount,
                               @Param("failCount") int failCount,
                               @Param("errorMessage") String errorMessage);

    List<EvAdminSimulationChargerDTO> findRecentBrokenChargers();
}
