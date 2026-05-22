package com.ev.controller.admin;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin/station")
public class EvAdminStationController {
	    
    // 충전소 목록 화면
    @GetMapping("/list")
    public String stationList() {
        return "admin/station/station_list";
    }
    
    // 충전소 등록/관리 화면
    @GetMapping("/manage")
    public String stationManage() {
        return "admin/station/station_manage";
    }
}