package com.ev.controller.admin;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin/stat/sales")
public class EvAdminSalesController {
	
    // 매출 통계 화면
    @GetMapping
    public String salesStat() {
        return "admin/sales/sales_stat";
    }
    
    // 충전소별 매출 순위 전체 화면
    @GetMapping("/station")
    public String salesStationList() {
        return "admin/sales/sales_station_list";
    }

    // 최근 매출 내역 전체 화면
    @GetMapping("/history")
    public String salesHistoryList() {
        return "admin/sales/sales_history_list";
    }
}