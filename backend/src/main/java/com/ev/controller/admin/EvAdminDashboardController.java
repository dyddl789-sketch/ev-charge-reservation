package com.ev.controller.admin;

import java.time.LocalDate;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ev.dto.admin.EvAdminDashboardDTO;
import com.ev.service.admin.EvAdminDashboardService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/*
 * React MIS 관리자 대시보드 REST API
 */
@Slf4j
@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class EvAdminDashboardController {

    private final EvAdminDashboardService evAdminDashboardService;

    // 관리자 대시보드 데이터 조회
    @GetMapping("/dashboard")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','OPERATOR','ENGINEER')")
    public EvAdminDashboardDTO dashboard(
            @RequestParam(value = "date", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate date) {

        log.info("@# EvAdminDashboardController.dashboard()");

        if (date == null) {
            date = LocalDate.now();
        }

        log.info("@# dashboard date => {}", date);

        return evAdminDashboardService.getDashboard(date);
    }
}
