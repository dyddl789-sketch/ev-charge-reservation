package com.ev.controller.user;

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
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

            if (!"운영중".equals(charger.getStationStatus())) {
                return ResponseEntity.badRequest().body(Map.of("message", "현재 운영중인 충전소가 아니므로 예약할 수 없습니다."));
            }

            if (!"사용가능".equals(charger.getChargerStatus())) {
                String message = "점검중".equals(charger.getChargerStatus())
                        ? "현재 점검중인 충전기입니다. 다른 충전기를 선택해 주세요."
                        : "고장".equals(charger.getChargerStatus())
                                ? "현재 고장 상태인 충전기입니다. 다른 충전기를 선택해 주세요."
                                : "현재 예약 가능한 충전기가 아닙니다.";

                return ResponseEntity.badRequest().body(Map.of("message", message));
            }

            /*
             * 시간 구간 기반 선점으로 변경했다.
             * 폼 진입 시에는 아직 프론트의 날짜/시간/충전량 계산이 확정되지 않았으므로
             * 충전기 전체를 선점하지 않고, 프론트에서 선택 시간 구간 기준으로 선점한다.
             */
            return ResponseEntity.ok(buildFormResponse(
                    charger,
                    memberId,
                    null,
                    "예약 조건을 선택하면 해당 시간 구간을 임시 선점합니다."
            ));
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

        /*
         * 충전소 기준 예약 진입에서는 사용 가능한 충전기가 있으면 첫 번째 충전기를 임시 선점한다.
         */
        for (EvReservationChargerDTO candidate : chargerList) {
            if (!"운영중".equals(candidate.getStationStatus()) || !"사용가능".equals(candidate.getChargerStatus())) {
                continue;
            }

            return ResponseEntity.ok(
                    buildFormResponse(
                            candidate,
                            memberId,
                            null,
                            "예약 조건을 선택하면 해당 시간 구간을 임시 선점합니다."
                    )
            );
        }

        /*
         * 사용 가능한 충전기가 0대여도 예약 화면에는 진입할 수 있어야 한다.
         * 이 경우 프론트에서 충전소 정보와 충전기 목록을 보여주고 예약 버튼만 비활성화한다.
         */
        EvReservationChargerDTO firstCharger = chargerList.get(0);

        return ResponseEntity.ok(
                buildFormResponse(
                        firstCharger,
                        memberId,
                        null,
                        "현재 예약 가능한 충전기가 없습니다."
                )
        );
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


    @GetMapping("/my/{reservationId}")
    public ResponseEntity<?> getMyReservationDetail(@PathVariable("reservationId") Long reservationId,
                                                    @AuthenticationPrincipal EvUserDetails userDetails) {
        log.info("@# EvReservationApiController.getMyReservationDetail() reservationId => {}", reservationId);

        if (userDetails == null) {
            return ResponseEntity.status(401).body(Map.of("message", "로그인이 필요합니다."));
        }

        try {
            EvReservationDTO reservation = evReservationService.getMyReservationDetail(reservationId, userDetails.getMemberId());
            return ResponseEntity.ok(reservation);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
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
                    "message", "예약 인증코드입니다. 인증은 예약 시작 5분 전부터 시작 후 5분까지 가능합니다."
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

    @PostMapping("/charging/simulation/complete")
    public ResponseEntity<?> completeChargingSimulation(@RequestBody Map<String, Object> request,
                                                        @AuthenticationPrincipal EvUserDetails userDetails) {
        log.info("@# EvReservationApiController.completeChargingSimulation() request => {}", request);

        if (userDetails == null) {
            return ResponseEntity.status(401).body(Map.of("message", "로그인이 필요합니다."));
        }

        try {
            Long reservationId = toLong(request.get("reservationId"));
            EvReservationDTO reservation = evReservationService.completeChargingSimulation(
                    reservationId,
                    userDetails.getMemberId()
            );

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "reservation", reservation,
                    "message", "충전 시뮬레이션이 완료되었습니다. 충전기가 사용가능 상태로 복구되었습니다."
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        } catch (Exception e) {
            log.error("@# charging simulation complete fail", e);
            return ResponseEntity.internalServerError().body(Map.of("success", false, "message", "충전 완료 처리 중 오류가 발생했습니다."));
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
        return buildFormResponse(
                charger,
                memberId,
                charger.getChargerId(),
                "충전기 임시 선점이 완료되었습니다."
        );
    }

    private Map<String, Object> buildFormResponse(EvReservationChargerDTO charger,
                                                  Long memberId,
                                                  Long holdChargerId,
                                                  String message) {
        List<EvReservationChargerDTO> chargerList = evReservationService.getReservationChargerList(charger.getStationId(), memberId);
        List<EvVehicleDTO> vehicleList = evReservationService.getVehicleList(memberId);

        Map<String, Object> result = new HashMap<>();
        result.put("charger", charger);
        result.put("chargerList", chargerList);
        result.put("vehicleList", vehicleList);
        result.put("holdChargerId", holdChargerId);
        result.put("message", message);

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
