package com.ev.controller.admin;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.ev.dto.admin.usage.EvAdminUsageStatDTO;
import com.ev.service.admin.EvAdminUsageService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller
@RequestMapping("/admin/usage")
@RequiredArgsConstructor
public class EvAdminUsageController {

    private final EvAdminUsageService evAdminUsageService;

    // 이용 통계 화면
    @GetMapping
    public String usageStat(
            @RequestParam(value = "startDate", required = false) String startDate,
            @RequestParam(value = "endDate", required = false) String endDate,
            Model model) {

        log.info("@# EvAdminUsageController.usageStat()");

        // 기간 값이 없으면 이번 달 1일 ~ 오늘 기준으로 조회
        if (startDate == null || startDate.isBlank()) {
            startDate = LocalDate.now()
                    .withDayOfMonth(1)
                    .format(DateTimeFormatter.ISO_DATE);
        }

        if (endDate == null || endDate.isBlank()) {
            endDate = LocalDate.now()
                    .format(DateTimeFormatter.ISO_DATE);
        }

        log.info("@# startDate => {}", startDate);
        log.info("@# endDate => {}", endDate);

        EvAdminUsageStatDTO usageStat =
                evAdminUsageService.getUsageStat(startDate, endDate);

        model.addAttribute("usageStat", usageStat);
        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", endDate);

        return "admin/usage/usage_stat";
    }
}