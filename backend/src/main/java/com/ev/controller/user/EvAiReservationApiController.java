package com.ev.controller.user;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ev.dao.user.EvAiChatDAO;
import com.ev.dto.reservation.EvReservationDTO;
import com.ev.dto.vehicle.EvVehicleDTO;
import com.ev.security.EvUserDetails;
import com.ev.service.user.EvReservationService;
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

    private static final int DEFAULT_RADIUS_METER = 30000;
    private static final int DEFAULT_LIMIT = 5;
    private static final Duration CANDIDATE_TTL = Duration.ofMinutes(10);
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private final EvAiChatDAO evAiChatDAO;
    private final EvVehicleService evVehicleService;
    private final EvReservationService reservationService;
    private final StringRedisTemplate stringRedisTemplate;
    private final ObjectMapper objectMapper;

    /*
     * AI 예약 후보 조회
     * 대표 차량 + 기본 출발지 + 사용가능 충전기 + 예약 시간 중복 제외 기준으로 조회한다.
     */
    @PostMapping("/prepare")
    public ResponseEntity<?> prepareAiReservation(@AuthenticationPrincipal EvUserDetails userDetails,
                                                  @RequestBody Map<String, Object> request) {
        log.info("@# EvAiReservationApiController.prepareAiReservation() request => {}", request);

        if (userDetails == null) {
            return ResponseEntity.status(401).body(Map.of("message", "로그인이 필요합니다."));
        }

        try {
            Long memberId = userDetails.getMemberId();
            LocalDateTime startDateTime = resolveStartDateTime(request);
            validateFutureStartTime(startDateTime);

            Integer currentSoc = toInteger(request.getOrDefault("currentSoc", 30));
            Integer targetSoc = toInteger(request.getOrDefault("targetSoc", 80));
            validateSoc(currentSoc, targetSoc);

            String sort = resolveSort(request);
            int radiusMeter = toInteger(request.getOrDefault("radiusMeter", DEFAULT_RADIUS_METER));
            int limit = toInteger(request.getOrDefault("limit", DEFAULT_LIMIT));

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

            Map<String, Object> defaultLocation = evAiChatDAO.findDefaultLocationForAi(memberId);
            if (defaultLocation == null || defaultLocation.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "message", "AI가 주변 충전소를 추천하려면 기본 출발지를 먼저 설정해야 합니다. 충전소 찾기 화면에서 출발지를 등록하고 기본으로 설정해주세요."
                ));
            }

            List<Map<String, Object>> rawCandidateList = evAiChatDAO.findAiReservationCandidates(
                    memberId,
                    startDateTime,
                    currentSoc,
                    targetSoc,
                    radiusMeter,
                    Math.max(1, Math.min(limit, 10)),
                    sort
            );

            List<Map<String, Object>> candidateList = normalizeCandidateList(rawCandidateList, startDateTime, currentSoc, targetSoc);

            if (candidateList.isEmpty()) {
                return ResponseEntity.ok(Map.of(
                        "success", false,
                        "message", buildEmptyCandidateMessage(defaultLocation, vehicle, startDateTime),
                        "sort", sort,
                        "location", defaultLocation,
                        "vehicle", vehicle,
                        "candidates", List.of()
                ));
            }

            saveCandidates(memberId, candidateList);

            String answer = buildCandidateAnswer(vehicle, defaultLocation, candidateList, sort, startDateTime);

            Map<String, Object> response = new LinkedHashMap<>();
            response.put("success", true);
            response.put("message", answer);
            response.put("sort", sort);
            response.put("location", defaultLocation);
            response.put("vehicle", vehicle);
            response.put("candidates", candidateList);
            response.put("ttlSeconds", CANDIDATE_TTL.toSeconds());

            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        } catch (Exception e) {
            log.error("@# ai reservation prepare fail", e);
            return ResponseEntity.internalServerError().body(Map.of(
                    "success", false,
                    "message", "AI 예약 후보 조회 중 오류가 발생했습니다. 기본 출발지, 대표 차량, 충전소 데이터를 확인해주세요."
            ));
        }
    }

    /*
     * AI 예약 후보 확정
     * Redis에 저장된 후보를 기준으로 실제 reservation 데이터를 생성한다.
     */
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
            Long chargerId = toLong(value(candidate, "chargerId", "chargerid", "charger_id"));
            Long vehicleId = toLong(value(candidate, "vehicleId", "vehicleid", "vehicle_id"));
            Integer currentSoc = toInteger(request.getOrDefault("currentSoc", value(candidate, "currentSoc", "current_soc")));
            Integer targetSoc = toInteger(request.getOrDefault("targetSoc", value(candidate, "targetSoc", "target_soc")));
            validateSoc(currentSoc, targetSoc);

            LocalDateTime startDateTime = resolveConfirmStartDateTime(request, candidate);
            validateFutureStartTime(startDateTime);

            Integer estimatedMinutes = toInteger(value(candidate, "estimatedMinutes", "estimatedminutes", "estimated_minutes"));
            if (estimatedMinutes == null || estimatedMinutes <= 0) {
                estimatedMinutes = 30;
            }
            LocalDateTime endDateTime = startDateTime.plusMinutes(estimatedMinutes);

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
                    + "충전소 : " + safe(reservation.getStationName()) + "\n"
                    + "충전기 : " + safe(reservation.getChargerName()) + "\n"
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

    private List<Map<String, Object>> normalizeCandidateList(List<Map<String, Object>> rawList,
                                                             LocalDateTime startDateTime,
                                                             Integer currentSoc,
                                                             Integer targetSoc) {
        List<Map<String, Object>> result = new ArrayList<>();

        if (rawList == null) {
            return result;
        }

        for (int i = 0; i < rawList.size(); i++) {
            Map<String, Object> raw = rawList.get(i);
            Map<String, Object> item = new LinkedHashMap<>();

            item.put("candidateNo", i + 1);
            item.put("vehicleId", value(raw, "vehicleId", "vehicleid", "vehicle_id"));
            item.put("vehicleNickname", value(raw, "vehicleNickname", "vehiclenickname", "vehicle_nickname"));
            item.put("manufacturer", value(raw, "manufacturer"));
            item.put("modelName", value(raw, "modelName", "modelname", "model_name"));
            item.put("batteryCapacityKwh", number(value(raw, "batteryCapacityKwh", "batterycapacitykwh", "battery_capacity_kwh")));
            item.put("vehicleConnectorType", value(raw, "vehicleConnectorType", "vehicleconnectortype", "vehicle_connector_type"));
            item.put("locationName", value(raw, "locationName", "locationname", "location_name"));
            item.put("locationAddress", value(raw, "locationAddress", "locationaddress", "location_address"));
            item.put("stationId", value(raw, "stationId", "stationid", "station_id"));
            item.put("stationName", value(raw, "stationName", "stationname", "station_name"));
            item.put("address", value(raw, "address"));
            item.put("operatorName", value(raw, "operatorName", "operatorname", "operator_name"));
            item.put("latitude", number(value(raw, "latitude")));
            item.put("longitude", number(value(raw, "longitude")));
            item.put("distanceKm", number(value(raw, "distanceKm", "distancekm", "distance_km")));
            item.put("chargerId", value(raw, "chargerId", "chargerid", "charger_id"));
            item.put("chargerName", value(raw, "chargerName", "chargername", "charger_name"));
            item.put("chargerType", value(raw, "chargerType", "chargertype", "charger_type"));
            item.put("connectorType", value(raw, "connectorType", "connectortype", "connector_type"));
            item.put("chargingSpeedKw", number(value(raw, "chargingSpeedKw", "chargingspeedkw", "charging_speed_kw")));
            item.put("effectiveChargingSpeedKw", number(value(raw, "effectiveChargingSpeedKw", "effectivechargingspeedkw", "effective_charging_speed_kw")));
            item.put("pricePerKwh", number(value(raw, "pricePerKwh", "priceperkwh", "price_per_kwh")));
            item.put("status", value(raw, "status"));
            item.put("requiredKwh", number(value(raw, "requiredKwh", "requiredkwh", "required_kwh")));
            item.put("estimatedMinutes", toInteger(value(raw, "estimatedMinutes", "estimatedminutes", "estimated_minutes")));
            item.put("estimatedCost", number(value(raw, "estimatedCost", "estimatedcost", "estimated_cost")));
            item.put("reservationDate", startDateTime.toLocalDate().toString());
            item.put("startTime", startDateTime.toLocalTime().withSecond(0).withNano(0).toString());
            item.put("startDateTime", startDateTime.toString());
            item.put("currentSoc", currentSoc);
            item.put("targetSoc", targetSoc);

            result.add(item);
        }

        return result;
    }

    private void saveCandidates(Long memberId, List<Map<String, Object>> candidateList) {
        try {
            String json = objectMapper.writeValueAsString(candidateList);
            stringRedisTemplate.opsForValue().set(candidateKey(memberId), json, CANDIDATE_TTL);
            log.info("@# ai reservation candidate saved => memberId: {}, count: {}", memberId, candidateList.size());
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

    private String buildCandidateAnswer(EvVehicleDTO vehicle,
                                        Map<String, Object> location,
                                        List<Map<String, Object>> candidateList,
                                        String sort,
                                        LocalDateTime startDateTime) {
        StringBuilder builder = new StringBuilder();
        builder.append("AI_RESERVATION_PREPARE\n");
        builder.append("DB에 저장된 기본 출발지와 대표 차량 기준으로 예약 가능한 충전기를 찾았습니다.\n");
        builder.append("기본 출발지 : ").append(text(value(location, "locationName", "location_name")))
                .append(" / ").append(text(value(location, "address"))).append("\n");
        builder.append("대표 차량 : ").append(safe(vehicle.getManufacturer())).append(" ")
                .append(safe(vehicle.getModelName())).append("\n");
        builder.append("예약 시간 : ").append(startDateTime.format(DATE_TIME_FORMATTER)).append("\n");

        if ("COST".equals(sort)) {
            builder.append("정렬 기준 : 가까운 후보 중 요금이 저렴한 순\n");
        } else if ("SPEED".equals(sort)) {
            builder.append("정렬 기준 : 가까운 후보 중 예상 충전 시간이 짧은 순\n");
        } else {
            builder.append("정렬 기준 : 사용 가능한 충전기 중 가까운 순\n");
        }

        for (Map<String, Object> candidate : candidateList) {
            builder.append("\n")
                    .append(candidate.get("candidateNo")).append("번. ")
                    .append(candidate.get("stationName")).append("\n")
                    .append("거리 : ").append(candidate.get("distanceKm")).append("km\n")
                    .append("충전기 : ").append(candidate.get("chargerName")).append("\n")
                    .append("커넥터 : ").append(candidate.get("connectorType")).append("\n")
                    .append("출력 : ").append(candidate.get("chargingSpeedKw")).append("kW")
                    .append(" / 실제 적용 속도 : ").append(candidate.get("effectiveChargingSpeedKw")).append("kW\n")
                    .append("요금 : ").append(candidate.get("pricePerKwh")).append("원/kWh\n")
                    .append("예상 시간 : ").append(candidate.get("estimatedMinutes")).append("분")
                    .append(" / 예상 비용 : ").append(Math.round(number(candidate.get("estimatedCost")))).append("원\n")
                    .append("주소 : ").append(candidate.get("address")).append("\n");
        }

        builder.append("\n후보 카드의 예약 버튼을 누르면 Redis에 저장된 후보 기준으로 실제 예약을 진행합니다.");
        return builder.toString();
    }

    private String buildEmptyCandidateMessage(Map<String, Object> location,
                                              EvVehicleDTO vehicle,
                                              LocalDateTime startDateTime) {
        return "현재 조건에 맞는 예약 가능 충전기를 찾지 못했습니다.\n"
                + "기본 출발지 : " + text(value(location, "locationName", "location_name")) + " / " + text(value(location, "address")) + "\n"
                + "대표 차량 : " + safe(vehicle.getManufacturer()) + " " + safe(vehicle.getModelName()) + "\n"
                + "예약 시간 : " + startDateTime.format(DATE_TIME_FORMATTER) + "\n"
                + "확인할 점 : 충전기 상태가 사용가능인지, 차량 커넥터 타입과 충전기 타입이 맞는지, 같은 시간대 예약이 이미 있는지 확인해주세요.";
    }

    private String resolveSort(Map<String, Object> request) {
        String sort = text(request.get("sort")).toUpperCase();
        if ("DISTANCE".equals(sort) || "COST".equals(sort) || "SPEED".equals(sort)) {
            return sort;
        }

        String message = text(request.get("message")) + " " + text(request.get("keyword"));
        String lower = message.toLowerCase();

        if (lower.contains("싼") || lower.contains("저렴") || lower.contains("요금") || lower.contains("비용") || lower.contains("가격")) {
            return "COST";
        }

        if (lower.contains("빠른") || lower.contains("급속") || lower.contains("초급속") || lower.contains("속도") || lower.contains("시간")) {
            return "SPEED";
        }

        return "DISTANCE";
    }

    private LocalDateTime resolveConfirmStartDateTime(Map<String, Object> request, Map<String, Object> candidate) {
        String requestStartDateTime = text(request.get("startDateTime"));
        if (!requestStartDateTime.isBlank()) {
            return LocalDateTime.parse(requestStartDateTime);
        }

        String dateText = text(request.get("reservationDate"));
        String timeText = text(request.get("startTime"));

        if (!dateText.isBlank() && !timeText.isBlank()) {
            return LocalDateTime.of(LocalDate.parse(dateText), LocalTime.parse(timeText));
        }

        String candidateStartDateTime = text(value(candidate, "startDateTime", "startdatetime", "start_date_time"));
        if (!candidateStartDateTime.isBlank()) {
            return LocalDateTime.parse(candidateStartDateTime);
        }

        return resolveStartDateTime(request);
    }

    private LocalDateTime resolveStartDateTime(Map<String, Object> request) {
        String startDateTime = text(request.get("startDateTime"));
        if (!startDateTime.isBlank()) {
            return LocalDateTime.parse(startDateTime);
        }

        String dateText = text(request.get("reservationDate"));
        String timeText = text(request.get("startTime"));

        LocalDate date = dateText.isBlank() ? LocalDate.now() : LocalDate.parse(dateText);
        LocalTime time = timeText.isBlank()
                ? LocalTime.now().plusHours(1).withMinute(0).withSecond(0).withNano(0)
                : LocalTime.parse(timeText);

        return LocalDateTime.of(date, time);
    }

    private void validateFutureStartTime(LocalDateTime startDateTime) {
        if (startDateTime == null) {
            throw new IllegalArgumentException("예약 시간을 선택하세요.");
        }

        if (startDateTime.isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("현재 시간보다 이전 시간으로 AI 예약을 진행할 수 없습니다.");
        }
    }

    private void validateSoc(Integer currentSoc, Integer targetSoc) {
        if (currentSoc == null || targetSoc == null) {
            throw new IllegalArgumentException("현재 SOC와 목표 SOC를 입력해주세요.");
        }

        if (currentSoc < 0 || currentSoc > 100 || targetSoc < 0 || targetSoc > 100) {
            throw new IllegalArgumentException("SOC는 0~100 사이로 입력해주세요.");
        }

        if (targetSoc <= currentSoc) {
            throw new IllegalArgumentException("목표 SOC는 현재 SOC보다 커야 합니다.");
        }
    }

    private Object value(Map<String, Object> map, String... keys) {
        if (map == null || keys == null) {
            return null;
        }

        for (String key : keys) {
            if (map.containsKey(key)) {
                return map.get(key);
            }
        }

        for (Map.Entry<String, Object> entry : map.entrySet()) {
            for (String key : keys) {
                if (entry.getKey() != null && entry.getKey().equalsIgnoreCase(key)) {
                    return entry.getValue();
                }
            }
        }

        return null;
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
        return value == null ? "" : String.valueOf(value).trim();
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }
}
