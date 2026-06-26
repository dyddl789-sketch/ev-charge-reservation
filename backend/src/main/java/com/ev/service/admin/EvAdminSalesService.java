package com.ev.service.admin;

import com.ev.dto.admin.sales.EvAdminSalesStatDTO;

public interface EvAdminSalesService {

    // 매출 통계 화면 데이터 조회
    EvAdminSalesStatDTO getSalesStat(String startDate, String endDate, String region, Long stationId, String chargerType);
}
