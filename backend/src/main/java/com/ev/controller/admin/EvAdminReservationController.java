package com.ev.controller.admin;

import java.time.LocalDate;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.ev.dto.admin.reservation.EvAdminReservationPageDTO;
import com.ev.dto.admin.reservation.EvAdminReservationSearchDTO;
import com.ev.service.admin.EvAdminReservationService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller
@RequestMapping("/admin/reservation")
@RequiredArgsConstructor
public class EvAdminReservationController {

    private final EvAdminReservationService evAdminReservationService;

    // 예약 현황 화면
    @GetMapping("/list")
    public String reservationList(
            @RequestParam(value = "status", required = false) String status,
            @RequestParam(value = "searchType", required = false) String searchType,
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "startDate", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate startDate,
            @RequestParam(value = "endDate", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate endDate,
            @RequestParam(value = "page", required = false, defaultValue = "1") Integer page,
            @RequestParam(value = "size", required = false, defaultValue = "10") Integer size,
            Model model) {

        log.info("@# EvAdminReservationController.reservationList()");

        EvAdminReservationSearchDTO searchDTO = new EvAdminReservationSearchDTO();

        searchDTO.setStatus(status);
        searchDTO.setSearchType(searchType);
        searchDTO.setKeyword(keyword);
        searchDTO.setStartDate(startDate);
        searchDTO.setEndDate(endDate);
        searchDTO.setPage(page);
        searchDTO.setSize(size);

        EvAdminReservationPageDTO reservationPage =
                evAdminReservationService.getReservationPage(searchDTO);

        model.addAttribute("searchDTO", searchDTO);
        model.addAttribute("reservationPage", reservationPage);

        return "admin/reservation/reservation_list";
    }

    // 예약 취소 처리
    @PostMapping("/cancel")
    public String cancelReservation(
            @RequestParam("reservationId") Long reservationId) {

        log.info("@# EvAdminReservationController.cancelReservation()");

        evAdminReservationService.cancelReservation(reservationId);

        return "redirect:/admin/reservation/list";
    }

    // 노쇼 처리
    @PostMapping("/noshow")
    public String noShowReservation(
            @RequestParam("reservationId") Long reservationId) {

        log.info("@# EvAdminReservationController.noShowReservation()");

        evAdminReservationService.noShowReservation(reservationId);

        return "redirect:/admin/reservation/list";
    }

    // 충전 시작 처리
    @PostMapping("/start")
    public String startCharging(
            @RequestParam("reservationId") Long reservationId) {

        log.info("@# EvAdminReservationController.startCharging()");

        evAdminReservationService.startCharging(reservationId);

        return "redirect:/admin/reservation/list";
    }
}