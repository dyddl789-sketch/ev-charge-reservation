package com.ev.controller.admin;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin")
public class EvAdminDashboardController {
	
    // 관리자 대시보드
    @GetMapping("/dashboard")
    public String dashboard() {
        return "admin/dashboard/admin_dashboard";
    }    
}