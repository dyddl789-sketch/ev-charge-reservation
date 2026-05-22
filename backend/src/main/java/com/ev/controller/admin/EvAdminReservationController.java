package com.ev.controller.admin;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin/reservation")
public class EvAdminReservationController {
	
    // 예약 현황 화면
    @GetMapping("/list")
    public String reservationList() {
        return "admin/reservation/reservation_list";
    }
    
    // 예약 상세 화면
    @GetMapping("/detail")
    public String reservationDetail() {
        return "admin/reservation/reservation_detail";
    }
}