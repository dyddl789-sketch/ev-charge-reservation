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
    public EvAdminSalesStatDTO getSalesStat(String startDate, String endDate) {

        log.info("@# EvAdminSalesServiceImpl.getSalesStat()");
        log.info("@# startDate => {}", startDate);
        log.info("@# endDate => {}", endDate);

        EvAdminSalesStatDTO salesStatDTO = new EvAdminSalesStatDTO();

        salesStatDTO.setSummary(
                evAdminSalesDAO.findSalesSummary(startDate, endDate)
        );

        salesStatDTO.setDailyList(
                evAdminSalesDAO.findDailySalesList(startDate, endDate)
        );

        salesStatDTO.setTypeList(
                evAdminSalesDAO.findSalesTypeList(startDate, endDate)
        );

        salesStatDTO.setStationRankList(
                evAdminSalesDAO.findStationSalesRankList(startDate, endDate)
        );

        salesStatDTO.setHistoryList(
                evAdminSalesDAO.findSalesHistoryList(startDate, endDate)
        );

        return salesStatDTO;
    }
}