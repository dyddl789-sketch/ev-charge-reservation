package com.ev.controller.admin;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ev.dto.admin.usage.EvAdminUsageStatDTO;
import com.ev.service.admin.EvAdminUsageService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/admin/usage")
@RequiredArgsConstructor
public class EvAdminUsageController {

    private final EvAdminUsageService evAdminUsageService;

    // 이용 통계 JSON API
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public EvAdminUsageStatDTO usageStat(
            @RequestParam(value = "startDate", required = false) String startDate,
            @RequestParam(value = "endDate", required = false) String endDate,
            @RequestParam(value = "region", required = false) String region,
            @RequestParam(value = "stationId", required = false) Long stationId,
            @RequestParam(value = "chargerType", required = false) String chargerType) {

        log.info("@# EvAdminUsageController.usageStat()");

        if (startDate == null || startDate.isBlank()) {
            startDate = LocalDate.now().withDayOfMonth(1).format(DateTimeFormatter.ISO_DATE);
        }

        if (endDate == null || endDate.isBlank()) {
            endDate = LocalDate.now().format(DateTimeFormatter.ISO_DATE);
        }

        log.info("@# startDate => {}, endDate => {}, region => {}, stationId => {}, chargerType => {}",
                startDate, endDate, region, stationId, chargerType);

        return evAdminUsageService.getUsageStat(startDate, endDate, region, stationId, chargerType);
    }
}
