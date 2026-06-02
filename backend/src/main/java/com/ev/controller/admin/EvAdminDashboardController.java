package com.ev.controller.admin;

import java.time.LocalDate;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.ev.dto.admin.EvAdminDashboardDTO;
import com.ev.service.admin.EvAdminDashboardService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class EvAdminDashboardController {

    private final EvAdminDashboardService evAdminDashboardService;

    // 관리자 대시보드
    @GetMapping("/dashboard")
    public String dashboard(
            @RequestParam(value = "date", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate date,
            Model model) {

        log.info("@# EvAdminDashboardController.dashboard()");

        // 날짜 선택값이 없으면 오늘 기준
        if (date == null) {
            date = LocalDate.now();
        }

        EvAdminDashboardDTO dashboardDTO =
                evAdminDashboardService.getDashboard(date);

        model.addAttribute("selectedDate", date);
        model.addAttribute("dashboard", dashboardDTO);

        return "admin/dashboard/admin_dashboard";
    }
}