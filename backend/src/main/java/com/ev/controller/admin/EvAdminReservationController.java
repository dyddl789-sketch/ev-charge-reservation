package com.ev.controller.admin;

import java.time.LocalDate;
import java.util.Map;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ev.dto.admin.reservation.EvAdminReservationDetailDTO;
import com.ev.dto.admin.reservation.EvAdminReservationPageDTO;
import com.ev.dto.admin.reservation.EvAdminReservationSearchDTO;
import com.ev.service.admin.EvAdminReservationService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/admin/reservation")
@RequiredArgsConstructor
public class EvAdminReservationController {

    private final EvAdminReservationService evAdminReservationService;

    // 예약 현황 JSON API
    @GetMapping("/list")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','OPERATOR')")
    public EvAdminReservationPageDTO reservationList(
            @RequestParam(value = "status", required = false) String status,
            @RequestParam(value = "searchType", required = false) String searchType,
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "startDate", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate startDate,
            @RequestParam(value = "endDate", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate endDate,
            @RequestParam(value = "startTime", required = false) String startTime,
            @RequestParam(value = "endTime", required = false) String endTime,
            @RequestParam(value = "page", required = false, defaultValue = "1") Integer page,
            @RequestParam(value = "size", required = false, defaultValue = "10") Integer size) {

        log.info("@# EvAdminReservationController.reservationList()");
        log.info("@# status => {}, keyword => {}, startDate => {}, endDate => {}, startTime => {}, endTime => {}",
                status, keyword, startDate, endDate, startTime, endTime);

        EvAdminReservationSearchDTO searchDTO = new EvAdminReservationSearchDTO();
        searchDTO.setStatus(status);
        searchDTO.setSearchType(searchType);
        searchDTO.setKeyword(keyword);
        searchDTO.setStartDate(startDate);
        searchDTO.setEndDate(endDate);
        searchDTO.setStartTime(startTime);
        searchDTO.setEndTime(endTime);
        searchDTO.setPage(page);
        searchDTO.setSize(size);

        return evAdminReservationService.getReservationPage(searchDTO);
    }

    // 예약 상세 JSON API
    @GetMapping("/detail")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','OPERATOR')")
    public EvAdminReservationDetailDTO reservationDetail(@RequestParam("reservationId") Long reservationId) {
        log.info("@# EvAdminReservationController.reservationDetail()");
        log.info("@# reservationId => {}", reservationId);
        return evAdminReservationService.getReservationDetail(reservationId);
    }

    // 예약 취소 처리 JSON API
    @PostMapping("/cancel")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','OPERATOR')")
    public Map<String, Object> cancelReservation(@RequestBody(required = false) Map<String, Object> body,
                                                 @RequestParam(value = "reservationId", required = false) Long requestParamReservationId) {
        Long reservationId = resolveLong(body, requestParamReservationId, "reservationId");

        log.info("@# EvAdminReservationController.cancelReservation()");
        log.info("@# reservationId => {}", reservationId);

        evAdminReservationService.cancelReservation(reservationId);

        return Map.of("success", true, "message", "예약이 취소되었습니다.", "reservationId", reservationId);
    }

    // 노쇼 처리 JSON API
    @PostMapping("/noshow")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','OPERATOR')")
    public Map<String, Object> noShowReservation(@RequestBody(required = false) Map<String, Object> body,
                                                 @RequestParam(value = "reservationId", required = false) Long requestParamReservationId) {
        Long reservationId = resolveLong(body, requestParamReservationId, "reservationId");

        log.info("@# EvAdminReservationController.noShowReservation()");
        log.info("@# reservationId => {}", reservationId);

        evAdminReservationService.noShowReservation(reservationId);

        return Map.of("success", true, "message", "노쇼 처리되었습니다.", "reservationId", reservationId);
    }

    // 충전 시작 처리 JSON API
    @PostMapping("/start")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','OPERATOR')")
    public Map<String, Object> startCharging(@RequestBody(required = false) Map<String, Object> body,
                                             @RequestParam(value = "reservationId", required = false) Long requestParamReservationId) {
        Long reservationId = resolveLong(body, requestParamReservationId, "reservationId");

        log.info("@# EvAdminReservationController.startCharging()");
        log.info("@# reservationId => {}", reservationId);

        evAdminReservationService.startCharging(reservationId);

        return Map.of("success", true, "message", "충전중 상태로 변경되었습니다.", "reservationId", reservationId);
    }

    private Long resolveLong(Map<String, Object> body, Long requestParamValue, String key) {
        if (requestParamValue != null) {
            return requestParamValue;
        }

        if (body == null || body.get(key) == null) {
            throw new IllegalArgumentException(key + " 값이 필요합니다.");
        }

        Object value = body.get(key);
        if (value instanceof Number number) {
            return number.longValue();
        }

        return Long.valueOf(String.valueOf(value));
    }
}
