package com.ev.controller.user;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ev.dto.reservation.EvReservationDTO;
import com.ev.dto.station.EvChargerDTO;
import com.ev.dto.station.EvStationMapDTO;
import com.ev.dto.vehicle.EvVehicleDTO;
import com.ev.security.EvUserDetails;
import com.ev.service.user.EvReservationService;
import com.ev.service.user.EvStationService;
import com.ev.service.user.EvVehicleService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/ai-chat/reservation")
@RequiredArgsConstructor
public class EvAiReservationApiController {

    private static final Duration CANDIDATE_TTL = Duration.ofMinutes(10);

    private final EvVehicleService evVehicleService;
    private final EvStationService stationService;
    private final EvReservationService reservationService;
    private final StringRedisTemplate stringRedisTemplate;
    private final ObjectMapper objectMapper;

    @PostMapping("/prepare")
    public ResponseEntity<?> prepareAiReservation(@AuthenticationPrincipal EvUserDetails userDetails,
                                                  @RequestBody Map<String, Object> request) {
        log.info("@# EvAiReservationApiController.prepareAiReservation() request => {}", request);

        if (userDetails == null) {
            return ResponseEntity.status(401).body(Map.of("message", "로그인이 필요합니다."));
        }

        Long memberId = userDetails.getMemberId();
        List<EvVehicleDTO> vehicleList = evVehicleService.getVehicleList(memberId);

        if (vehicleList == null || vehicleList.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "AI 예약을 사용하려면 먼저 차량을 등록해주세요."
            ));
        }

        EvVehicleDTO vehicle = vehicleList.stream()
                .filter(v -> Boolean.TRUE.equals(v.getIsDefault()))
                .findFirst()
                .orElse(vehicleList.get(0));

        String keyword = text(request.get("keyword"));
        String message = text(request.get("message"));
        String sort = resolveSort(message + " " + keyword);

        List<EvStationMapDTO> stationList = stationService.getStationMapList(keyword.isBlank() ? null : keyword);
        List<Map<String, Object>> candidateList = new ArrayList<>();

        for (EvStationMapDTO station : stationList) {
            if (station.getStationId() == null) {
                continue;
            }

            List<EvChargerDTO> chargerList = stationService.getChargerList(station.getStationId());

            for (EvChargerDTO charger : chargerList) {
                if (!"사용가능".equals(charger.getStatus())) {
                    continue;
                }

                if (!connectorMatches(vehicle.getConnectorType(), charger.getConnectorType())) {
                    continue;
                }

                Map<String, Object> candidate = new HashMap<>();
                candidate.put("candidateNo", candidateList.size() + 1);
                candidate.put("vehicleId", vehicle.getVehicleId());
                candidate.put("vehicleNickname", vehicle.getVehicleNickname());
                candidate.put("modelName", vehicle.getModelName());
                candidate.put("batteryCapacityKwh", vehicle.getBatteryCapacityKwh());
                candidate.put("vehicleConnectorType", vehicle.getConnectorType());
                candidate.put("stationId", station.getStationId());
                candidate.put("stationName", station.getStationName());
                candidate.put("address", station.getAddress());
                candidate.put("operatorName", station.getOperatorName());
                candidate.put("latitude", station.getLatitude());
                candidate.put("longitude", station.getLongitude());
                candidate.put("distanceKm", station.getDistanceKm());
                candidate.put("chargerId", charger.getChargerId());
                candidate.put("chargerName", charger.getChargerName());
                candidate.put("chargerType", charger.getChargerType());
                candidate.put("connectorType", charger.getConnectorType());
                candidate.put("chargingSpeedKw", toDouble(charger.getChargingSpeedKw()));
                candidate.put("pricePerKwh", toDouble(charger.getPricePerKwh()));
                candidate.put("status", charger.getStatus());

                candidateList.add(candidate);
            }
        }

        sortCandidates(candidateList, sort);

        for (int i = 0; i < candidateList.size(); i++) {
            candidateList.get(i).put("candidateNo", i + 1);
        }

        if (candidateList.size() > 5) {
            candidateList = new ArrayList<>(candidateList.subList(0, 5));
        }

        if (candidateList.isEmpty()) {
            return ResponseEntity.ok(Map.of(
                    "success", false,
                    "message", "현재 대표 차량과 맞는 예약 가능 충전기를 찾지 못했습니다. 차량 커넥터 타입 또는 충전소 데이터를 확인해주세요.",
                    "candidates", List.of()
            ));
        }

        saveCandidates(memberId, candidateList);

        String answer = buildCandidateAnswer(vehicle, candidateList, sort);

        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", answer,
                "sort", sort,
                "vehicle", vehicle,
                "candidates", candidateList
        ));
    }

    @PostMapping("/confirm")
    public ResponseEntity<?> confirmAiReservation(@AuthenticationPrincipal EvUserDetails userDetails,
                                                  @RequestBody Map<String, Object> request) {
        log.info("@# EvAiReservationApiController.confirmAiReservation() request => {}", request);

        if (userDetails == null) {
            return ResponseEntity.status(401).body(Map.of("message", "로그인이 필요합니다."));
        }

        Long memberId = userDetails.getMemberId();
        List<Map<String, Object>> candidateList = readCandidates(memberId);

        if (candidateList.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "AI 예약 후보가 만료되었습니다. 다시 추천을 요청해주세요."
            ));
        }

        Integer candidateNo = toInteger(request.getOrDefault("candidateNo", 1));
        if (candidateNo == null || candidateNo < 1 || candidateNo > candidateList.size()) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "선택한 예약 후보 번호를 확인해주세요."
            ));
        }

        Map<String, Object> candidate = candidateList.get(candidateNo - 1);

        try {
            Long chargerId = toLong(candidate.get("chargerId"));
            Long vehicleId = toLong(candidate.get("vehicleId"));
            Integer currentSoc = toInteger(request.getOrDefault("currentSoc", 30));
            Integer targetSoc = toInteger(request.getOrDefault("targetSoc", 80));

            LocalDateTime startDateTime = resolveStartDateTime(request);
            LocalDateTime endDateTime = startDateTime.plusMinutes(
                    calculateEstimatedMinutes(candidate, currentSoc, targetSoc)
            );

            boolean holdSuccess = reservationService.holdChargerForReservation(chargerId, memberId);
            if (!holdSuccess) {
                return ResponseEntity.status(409).body(Map.of(
                        "success", false,
                        "message", "다른 사용자가 해당 충전기를 선택 중입니다. 다시 추천을 요청해주세요."
                ));
            }

            EvReservationDTO reservationDTO = new EvReservationDTO();
            reservationDTO.setMemberId(memberId);
            reservationDTO.setVehicleId(vehicleId);
            reservationDTO.setChargerId(chargerId);
            reservationDTO.setStartTime(startDateTime);
            reservationDTO.setEndTime(endDateTime);
            reservationDTO.setCurrentSoc(currentSoc);
            reservationDTO.setTargetSoc(targetSoc);

            Long reservationId = reservationService.createReservation(reservationDTO);
            EvReservationDTO reservation = reservationService.getReservationComplete(reservationId, memberId);

            stringRedisTemplate.delete(candidateKey(memberId));

            String message = "AI 예약이 완료되었습니다.\n"
                    + "예약번호 : " + reservationId + "\n"
                    + "충전소 : " + reservation.getStationName() + "\n"
                    + "충전기 : " + reservation.getChargerName() + "\n"
                    + "예약시간 : " + reservation.getStartTimeText() + " ~ " + reservation.getEndTimeText() + "\n"
                    + "예상 비용 : " + Math.round(reservation.getEstimatedCost()) + "원";

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", message,
                    "reservationId", reservationId,
                    "reservation", reservation
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        } catch (Exception e) {
            log.error("@# ai reservation confirm fail", e);
            return ResponseEntity.internalServerError().body(Map.of("success", false, "message", "AI 예약 확정 중 오류가 발생했습니다."));
        }
    }

    private void saveCandidates(Long memberId, List<Map<String, Object>> candidateList) {
        try {
            String json = objectMapper.writeValueAsString(candidateList);
            stringRedisTemplate.opsForValue().set(candidateKey(memberId), json, CANDIDATE_TTL);
        } catch (Exception e) {
            log.warn("@# ai reservation candidate save fail => {}", e.getMessage());
        }
    }

    private List<Map<String, Object>> readCandidates(Long memberId) {
        try {
            String json = stringRedisTemplate.opsForValue().get(candidateKey(memberId));
            if (json == null || json.isBlank()) {
                return List.of();
            }

            return objectMapper.readValue(json, new TypeReference<List<Map<String, Object>>>() {});
        } catch (Exception e) {
            log.warn("@# ai reservation candidate read fail => {}", e.getMessage());
            return List.of();
        }
    }

    private String candidateKey(Long memberId) {
        return "ai:reservation:candidate:" + memberId;
    }

    private String buildCandidateAnswer(EvVehicleDTO vehicle, List<Map<String, Object>> candidateList, String sort) {
        StringBuilder builder = new StringBuilder();
        builder.append("대표 차량 ")
                .append(vehicle.getManufacturer()).append(" ")
                .append(vehicle.getModelName()).append(" 기준으로 예약 가능한 충전기를 찾았습니다.\n");

        if ("COST".equals(sort)) {
            builder.append("요금이 낮은 순서로 안내합니다.\n");
        } else if ("SPEED".equals(sort)) {
            builder.append("충전 속도가 빠른 순서로 안내합니다.\n");
        } else {
            builder.append("현재 DB에 저장된 사용 가능 충전기 기준으로 안내합니다.\n");
        }

        for (Map<String, Object> candidate : candidateList) {
            builder.append("\n")
                    .append(candidate.get("candidateNo")).append("번. ")
                    .append(candidate.get("stationName")).append("\n")
                    .append("충전기 : ").append(candidate.get("chargerName")).append("\n")
                    .append("타입 : ").append(candidate.get("chargerType")).append(" / ").append(candidate.get("connectorType")).append("\n")
                    .append("출력 : ").append(candidate.get("chargingSpeedKw")).append("kW\n")
                    .append("요금 : ").append(candidate.get("pricePerKwh")).append("원/kWh\n")
                    .append("주소 : ").append(candidate.get("address")).append("\n");
        }

        builder.append("\n원하는 후보의 예약 버튼을 누르면 실제 예약을 진행합니다.");
        return builder.toString();
    }

    private void sortCandidates(List<Map<String, Object>> candidateList, String sort) {
        if ("COST".equals(sort)) {
            candidateList.sort(Comparator.comparingDouble(c -> number(c.get("pricePerKwh"))));
            return;
        }

        if ("SPEED".equals(sort)) {
            candidateList.sort(Comparator.comparingDouble((Map<String, Object> c) -> number(c.get("chargingSpeedKw"))).reversed());
        }
    }

    private String resolveSort(String message) {
        String lower = message == null ? "" : message.toLowerCase();

        if (lower.contains("싼") || lower.contains("저렴") || lower.contains("요금") || lower.contains("비용")) {
            return "COST";
        }

        if (lower.contains("빠른") || lower.contains("급속") || lower.contains("초급속") || lower.contains("속도")) {
            return "SPEED";
        }

        return "DEFAULT";
    }

    private boolean connectorMatches(String vehicleConnector, String chargerConnector) {
        if (vehicleConnector == null || chargerConnector == null) {
            return true;
        }

        String vehicleValue = normalizeConnector(vehicleConnector);
        String chargerValue = normalizeConnector(chargerConnector);

        return chargerValue.contains(vehicleValue) || vehicleValue.contains(chargerValue);
    }

    private String normalizeConnector(String value) {
        return value.replace(" ", "").replace("_", "").toUpperCase();
    }

    private LocalDateTime resolveStartDateTime(Map<String, Object> request) {
        String startDateTime = text(request.get("startDateTime"));
        if (!startDateTime.isBlank()) {
            return LocalDateTime.parse(startDateTime);
        }

        String dateText = text(request.get("reservationDate"));
        String timeText = text(request.get("startTime"));

        LocalDate date = dateText.isBlank() ? LocalDate.now() : LocalDate.parse(dateText);
        LocalTime time = timeText.isBlank() ? LocalTime.now().plusHours(1).withMinute(0).withSecond(0).withNano(0) : LocalTime.parse(timeText);

        return LocalDateTime.of(date, time);
    }

    private int calculateEstimatedMinutes(Map<String, Object> candidate, Integer currentSoc, Integer targetSoc) {
        double battery = number(candidate.get("batteryCapacityKwh"));
        double speed = number(candidate.get("chargingSpeedKw"));

        if (battery <= 0 || speed <= 0 || currentSoc == null || targetSoc == null || targetSoc <= currentSoc) {
            return 30;
        }

        double requiredKwh = battery * (targetSoc - currentSoc) / 100.0;
        return Math.max(10, (int) Math.ceil((requiredKwh / speed) * 60));
    }

    private double toDouble(BigDecimal value) {
        return value == null ? 0.0 : value.doubleValue();
    }

    private double number(Object value) {
        if (value == null) {
            return 0.0;
        }

        if (value instanceof Number number) {
            return number.doubleValue();
        }

        try {
            return Double.parseDouble(String.valueOf(value));
        } catch (Exception e) {
            return 0.0;
        }
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

    private String text(Object value) {
        if (value == null) {
            return "";
        }

        return String.valueOf(value).trim();
    }
}
