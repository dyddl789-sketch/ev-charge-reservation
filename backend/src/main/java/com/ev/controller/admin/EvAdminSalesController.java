package com.ev.controller.admin;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.ev.dto.admin.sales.EvAdminSalesStatDTO;
import com.ev.service.admin.EvAdminSalesService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller
@RequestMapping("/admin/sales")
@RequiredArgsConstructor
public class EvAdminSalesController {

    private final EvAdminSalesService evAdminSalesService;

    // 매출 통계 화면
    @GetMapping
    public String salesStat(
            @RequestParam(value = "startDate", required = false) String startDate,
            @RequestParam(value = "endDate", required = false) String endDate,
            Model model) {

        log.info("@# EvAdminSalesController.salesStat()");

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

        EvAdminSalesStatDTO salesStat =
                evAdminSalesService.getSalesStat(startDate, endDate);

        model.addAttribute("salesStat", salesStat);
        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", endDate);

        return "admin/sales/sales_stat";
    }
}