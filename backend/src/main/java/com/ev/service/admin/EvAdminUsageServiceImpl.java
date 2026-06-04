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
    public EvAdminUsageStatDTO getUsageStat(String startDate, String endDate) {

        log.info("@# EvAdminUsageServiceImpl.getUsageStat()");
        log.info("@# startDate => {}", startDate);
        log.info("@# endDate => {}", endDate);

        EvAdminUsageStatDTO usageStatDTO = new EvAdminUsageStatDTO();

        usageStatDTO.setSummary(
                evAdminUsageDAO.findUsageSummary(startDate, endDate)
        );

        usageStatDTO.setDailyList(
                evAdminUsageDAO.findDailyUsageList(startDate, endDate)
        );

        usageStatDTO.setHourlyList(
                evAdminUsageDAO.findHourlyUsageList(startDate, endDate)
        );

        usageStatDTO.setTypeList(
                evAdminUsageDAO.findUsageTypeList(startDate, endDate)
        );

        usageStatDTO.setStationRankList(
                evAdminUsageDAO.findStationUsageRankList(startDate, endDate)
        );

        return usageStatDTO;
    }
}