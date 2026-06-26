package com.ev.service.admin;

import org.springframework.stereotype.Service;

import com.ev.dao.admin.EvAdminSalesDAO;
import com.ev.dto.admin.sales.EvAdminSalesStatDTO;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class EvAdminSalesServiceImpl implements EvAdminSalesService {

    private final EvAdminSalesDAO evAdminSalesDAO;

    @Override
    public EvAdminSalesStatDTO getSalesStat(String startDate, String endDate, String region, Long stationId, String chargerType) {

        log.info("@# EvAdminSalesServiceImpl.getSalesStat()");
        log.info("@# startDate => {}, endDate => {}, region => {}, stationId => {}, chargerType => {}",
                startDate, endDate, region, stationId, chargerType);

        EvAdminSalesStatDTO salesStatDTO = new EvAdminSalesStatDTO();

        salesStatDTO.setSummary(evAdminSalesDAO.findSalesSummary(startDate, endDate, region, stationId, chargerType));
        salesStatDTO.setDailyList(evAdminSalesDAO.findDailySalesList(startDate, endDate, region, stationId, chargerType));
        salesStatDTO.setHourlyList(evAdminSalesDAO.findHourlySalesList(startDate, endDate, region, stationId, chargerType));
        salesStatDTO.setTypeList(evAdminSalesDAO.findSalesTypeList(startDate, endDate, region, stationId, chargerType));
        salesStatDTO.setStationRankList(evAdminSalesDAO.findStationSalesRankList(startDate, endDate, region, stationId, chargerType));
        salesStatDTO.setHistoryList(evAdminSalesDAO.findSalesHistoryList(startDate, endDate, region, stationId, chargerType));

        return salesStatDTO;
    }
}
