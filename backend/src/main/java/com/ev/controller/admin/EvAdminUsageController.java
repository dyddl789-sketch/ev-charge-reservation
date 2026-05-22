package com.ev.controller.admin;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin/stat/usage")
public class EvAdminUsageController {
	 
    // 이용 통계 화면
    @GetMapping
    public String usageStat() {
        return "admin/usage/usage_stat";
    }
    
    // 충전소별 이용 순위 전체 화면
    @GetMapping("/station")
    public String usageStationList() {
        return "admin/usage/usage_station_list";
    }
    
    // 최근 이용 내역 전체 페이지
    @GetMapping("/history")
    public String usageHistoryList() {
        return "admin/usage/usage_history_list";
    }
}