package com.ev.controller.user;

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ev.dto.reservation.EvReservationChargerDTO;
import com.ev.dto.reservation.EvReservationDTO;
import com.ev.dto.vehicle.EvVehicleDTO;
import com.ev.security.EvUserDetails;
import com.ev.service.user.EvReservationService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/reservation/api")
@RequiredArgsConstructor
public class EvReservationApiController {

    private final EvReservationService evReservationService;

    @GetMapping("/form")
    public ResponseEntity<?> getReservationForm(@RequestParam("chargerId") Long chargerId,
                                                @AuthenticationPrincipal EvUserDetails userDetails) {
        log.info("@# EvReservationApiController.getReservationForm() chargerId => {}", chargerId);

        if (userDetails == null) {
            return ResponseEntity.status(401).body(Map.of("message", "로그인이 필요합니다."));
        }

        Long memberId = userDetails.getMemberId();

        try {
            EvReservationChargerDTO charger = evReservationService.getReservationCharger(chargerId);

            if (!"사용가능".equals(charger.getChargerStatus())) {
                return ResponseEntity.badRequest().body(Map.of("message", "현재 예약 가능한 충전기가 아닙니다."));
            }

            boolean holdSuccess = evReservationService.holdChargerForReservation(chargerId, memberId);
            if (!holdSuccess) {
                return ResponseEntity.status(409).body(Map.of("message", "다른 사용자가 선택 중인 충전기입니다."));
            }

            return ResponseEntity.ok(buildFormResponse(charger, memberId));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @GetMapping("/form/station")
    public ResponseEntity<?> getReservationFormByStation(@RequestParam("stationId") Long stationId,
                                                         @AuthenticationPrincipal EvUserDetails userDetails) {
        log.info("@# EvReservationApiController.getReservationFormByStation() stationId => {}", stationId);

        if (userDetails == null) {
            return ResponseEntity.status(401).body(Map.of("message", "로그인이 필요합니다."));
        }

        Long memberId = userDetails.getMemberId();
        List<EvReservationChargerDTO> chargerList = evReservationService.getReservationChargerList(stationId, memberId);

        if (chargerList == null || chargerList.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("message", "등록된 충전기가 없습니다."));
        }

        for (EvReservationChargerDTO candidate : chargerList) {
            if (!"사용가능".equals(candidate.getChargerStatus())) {
                continue;
            }

            boolean holdSuccess = evReservationService.holdChargerForReservation(candidate.getChargerId(), memberId);
            if (holdSuccess) {
                return ResponseEntity.ok(buildFormResponse(candidate, memberId));
            }
        }

        return ResponseEntity.status(409).body(Map.of("message", "현재 예약 가능한 충전기가 없거나 다른 사용자가 선택 중입니다."));
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerReservation(@RequestBody Map<String, Object> request,
                                                 @AuthenticationPrincipal EvUserDetails userDetails) {
        log.info("@# EvReservationApiController.registerReservation() request => {}", request);

        if (userDetails == null) {
            return ResponseEntity.status(401).body(Map.of("message", "로그인이 필요합니다."));
        }

        try {
            EvReservationDTO reservationDTO = toReservationDTO(request);
            reservationDTO.setMemberId(userDetails.getMemberId());

            Long reservationId = evReservationService.createReservation(reservationDTO);
            EvReservationDTO reservation = evReservationService.getReservationComplete(reservationId, userDetails.getMemberId());

            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("reservationId", reservationId);
            result.put("reservation", reservation);
            result.put("message", "예약이 완료되었습니다.");

            return ResponseEntity.ok(result);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        } catch (Exception e) {
            log.error("@# reservation api register fail", e);
            return ResponseEntity.internalServerError().body(Map.of("success", false, "message", "예약 등록 중 오류가 발생했습니다."));
        }
    }

    @GetMapping("/complete")
    public ResponseEntity<?> getReservationComplete(@RequestParam("reservationId") Long reservationId,
                                                    @AuthenticationPrincipal EvUserDetails userDetails) {
        log.info("@# EvReservationApiController.getReservationComplete() reservationId => {}", reservationId);

        if (userDetails == null) {
            return ResponseEntity.status(401).body(Map.of("message", "로그인이 필요합니다."));
        }

        EvReservationDTO reservation = evReservationService.getReservationComplete(reservationId, userDetails.getMemberId());

        if (reservation == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(reservation);
    }

    @GetMapping("/my")
    public ResponseEntity<?> getMyReservationList(@RequestParam(value = "month", required = false) String month,
                                                  @AuthenticationPrincipal EvUserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(401).body(Map.of("message", "로그인이 필요합니다."));
        }

        LocalDateTime[] range = resolveMonthRange(month);
        return ResponseEntity.ok(evReservationService.getMyReservationList(userDetails.getMemberId(), range[0], range[1]));
    }

    @GetMapping("/history")
    public ResponseEntity<?> getChargingHistoryList(@RequestParam(value = "month", required = false) String month,
                                                    @AuthenticationPrincipal EvUserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(401).body(Map.of("message", "로그인이 필요합니다."));
        }

        LocalDateTime[] range = resolveMonthRange(month);
        return ResponseEntity.ok(evReservationService.getChargingHistoryList(userDetails.getMemberId(), range[0], range[1]));
    }

    @PostMapping("/cancel")
    public ResponseEntity<?> cancelReservation(@RequestBody Map<String, Object> request,
                                               @AuthenticationPrincipal EvUserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(401).body(Map.of("message", "로그인이 필요합니다."));
        }

        try {
            Long reservationId = toLong(request.get("reservationId"));
            evReservationService.cancelReservation(reservationId, userDetails.getMemberId());
            return ResponseEntity.ok(Map.of("success", true, "message", "예약이 취소되었습니다."));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    @PostMapping("/auth-code/issue")
    public ResponseEntity<?> issueAuthCode(@RequestBody Map<String, Object> request,
                                           @AuthenticationPrincipal EvUserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(401).body(Map.of("message", "로그인이 필요합니다."));
        }

        try {
            Long reservationId = toLong(request.get("reservationId"));
            String authCode = evReservationService.issueAuthCode(reservationId, userDetails.getMemberId());

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "reservationId", reservationId,
                    "authCode", authCode,
                    "message", "인증코드가 발급되었습니다. 5분 안에 입력해주세요."
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    @PostMapping("/verify")
    public ResponseEntity<?> verifyReservation(@RequestBody Map<String, Object> request,
                                               @AuthenticationPrincipal EvUserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(401).body(Map.of("message", "로그인이 필요합니다."));
        }

        try {
            Long reservationId = toLong(request.get("reservationId"));
            String authCode = String.valueOf(request.getOrDefault("authCode", "")).trim();

            evReservationService.verifyReservation(reservationId, userDetails.getMemberId(), authCode);

            return ResponseEntity.ok(Map.of("success", true, "message", "예약 인증이 완료되었습니다."));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    @PostMapping("/receipt/email")
    public ResponseEntity<?> sendReceiptEmail(@RequestBody Map<String, Object> request,
                                              @AuthenticationPrincipal EvUserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(401).body(Map.of("message", "로그인이 필요합니다."));
        }

        try {
            Long reservationId = toLong(request.get("reservationId"));
            evReservationService.sendReceiptEmail(reservationId, userDetails.getMemberId());
            return ResponseEntity.ok(Map.of("success", true, "message", "영수증 이메일 발송을 요청했습니다."));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        } catch (Exception e) {
            log.error("@# receipt email api fail", e);
            return ResponseEntity.internalServerError().body(Map.of("success", false, "message", "영수증 이메일 발송 중 오류가 발생했습니다."));
        }
    }

    private Map<String, Object> buildFormResponse(EvReservationChargerDTO charger, Long memberId) {
        List<EvReservationChargerDTO> chargerList = evReservationService.getReservationChargerList(charger.getStationId(), memberId);
        List<EvVehicleDTO> vehicleList = evReservationService.getVehicleList(memberId);

        Map<String, Object> result = new HashMap<>();
        result.put("charger", charger);
        result.put("chargerList", chargerList);
        result.put("vehicleList", vehicleList);
        result.put("holdChargerId", charger.getChargerId());
        result.put("message", "충전기 임시 선점이 완료되었습니다.");

        return result;
    }

    private EvReservationDTO toReservationDTO(Map<String, Object> request) {
        EvReservationDTO dto = new EvReservationDTO();
        dto.setVehicleId(toLong(request.get("vehicleId")));
        dto.setChargerId(toLong(request.get("chargerId")));
        dto.setStartTime(LocalDateTime.parse(String.valueOf(request.get("startTime"))));
        dto.setEndTime(LocalDateTime.parse(String.valueOf(request.get("endTime"))));
        dto.setCurrentSoc(toInteger(request.get("currentSoc")));
        dto.setTargetSoc(toInteger(request.get("targetSoc")));
        return dto;
    }

    private LocalDateTime[] resolveMonthRange(String month) {
        LocalDateTime startDate = null;
        LocalDateTime endDate = null;

        if (month != null && !month.isBlank()) {
            YearMonth yearMonth = YearMonth.parse(month);
            startDate = yearMonth.atDay(1).atStartOfDay();
            endDate = yearMonth.plusMonths(1).atDay(1).atStartOfDay();
        }

        return new LocalDateTime[] { startDate, endDate };
    }

    private Long toLong(Object value) {
        if (value == null || String.valueOf(value).isBlank()) {
            return null;
        }

        if (value instanceof Number number) {
            return number.longValue();
        }

        return Long.parseLong(String.valueOf(value));
    }

    private Integer toInteger(Object value) {
        if (value == null || String.valueOf(value).isBlank()) {
            return null;
        }

        if (value instanceof Number number) {
            return number.intValue();
        }

        return Integer.parseInt(String.valueOf(value));
    }
}
