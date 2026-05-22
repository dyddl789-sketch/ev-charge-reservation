package com.ev.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin")
public class AdminController {

    // 관리자 대시보드
    @GetMapping("/dashboard")
    public String dashboard() {
        return "admin/admin_dashboard";
    }
    
    // 회원 목록 화면
    @GetMapping("/member/list")
    public String memberList() {
        return "admin/member_list";
    }
    
    // 충전소 목록 화면
    @GetMapping("/station/list")
    public String stationList() {
        return "admin/station_list";
    }
    
    // 충전소 등록/관리 화면
    @GetMapping("/station/manage")
    public String stationManage() {
        return "admin/station_manage";
    }
    
    //예약 현황 화면
    @GetMapping("/reservation/list")
    public String reservationList() {
        return "admin/reservation_list";
    }
    
    // 예약 상세 화면
    @GetMapping("/reservation/detail")
    public String reservationDetail() {
        return "admin/reservation_detail";
    }
    
    // 매출 통계 화면
    @GetMapping("/stat/sales")
    public String salesStat() {
        return "admin/sales_stat";
    }
    
    // 충전소별 매출 순위 전체 화면
    @GetMapping("/stat/sales/station")
    public String salesStationList() {
        return "admin/sales_station_list";
    }

    // 최근 매출 내역 전체 화면
    @GetMapping("/stat/sales/history")
    public String salesHistoryList() {
        return "admin/sales_history_list";
    }
    
    // 이용 통계 화면
    @GetMapping("/stat/usage")
    public String usageStat() {
        return "admin/usage_stat";
    }
    
    // 충전소별 이용 순위 전체 화면
    @GetMapping("/stat/usage/station")
    public String usageStationList() {
        return "admin/usage_station_list";
    }
    
    // 최근 이용 내역 전체 페이지
    @GetMapping("/stat/usage/history")
    public String usageHistoryList() {
        return "admin/usage_history_list";
    }
    
    
    
}


