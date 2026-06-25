package com.ev.service.admin;

import org.springframework.stereotype.Service;

import com.ev.dao.admin.EvAdminUsageDAO;
import com.ev.dto.admin.usage.EvAdminUsageStatDTO;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class EvAdminUsageServiceImpl implements EvAdminUsageService {

    private final EvAdminUsageDAO evAdminUsageDAO;

    @Override
    public EvAdminUsageStatDTO getUsageStat(String startDate, String endDate, String region, Long stationId, String chargerType) {

        log.info("@# EvAdminUsageServiceImpl.getUsageStat()");
        log.info("@# startDate => {}, endDate => {}, region => {}, stationId => {}, chargerType => {}",
                startDate, endDate, region, stationId, chargerType);

        EvAdminUsageStatDTO usageStatDTO = new EvAdminUsageStatDTO();

        usageStatDTO.setSummary(evAdminUsageDAO.findUsageSummary(startDate, endDate, region, stationId, chargerType));
        usageStatDTO.setDailyList(evAdminUsageDAO.findDailyUsageList(startDate, endDate, region, stationId, chargerType));
        usageStatDTO.setHourlyList(evAdminUsageDAO.findHourlyUsageList(startDate, endDate, region, stationId, chargerType));
        usageStatDTO.setTypeList(evAdminUsageDAO.findUsageTypeList(startDate, endDate, region, stationId, chargerType));
        usageStatDTO.setStationRankList(evAdminUsageDAO.findStationUsageRankList(startDate, endDate, region, stationId, chargerType));

        return usageStatDTO;
    }
}
