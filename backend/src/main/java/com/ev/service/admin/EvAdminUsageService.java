package com.ev.service.admin;

import com.ev.dto.admin.usage.EvAdminUsageStatDTO;

public interface EvAdminUsageService {

    // 이용 통계 화면 데이터 조회
    EvAdminUsageStatDTO getUsageStat(String startDate, String endDate, String region, Long stationId, String chargerType);
}
