package com.ev.service.user;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import com.ev.dao.user.EvAiChatDAO;
import com.ev.dto.chat.EvAiChargeInfoDTO;
import com.ev.dto.chat.EvAiChatIntentDTO;
import com.ev.dto.chat.EvAiChatMessageDTO;
import com.ev.dto.chat.EvAiChatResponseDTO;
import com.ev.dto.chat.EvAiChatRoomDTO;
import com.ev.dto.chat.EvAiStationRecommendDTO;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@SuppressWarnings("unchecked")
public class EvAiChatServiceImpl implements EvAiChatService {

    private static final int CHAT_CACHE_LIMIT = 20;
    private static final Duration CHAT_CACHE_TTL = Duration.ofHours(24);
    private static final double CHARGING_EFFICIENCY = 1.15;

    private final EvAiChatDAO evAiChatDAO;
    private final StringRedisTemplate stringRedisTemplate;
    private final ObjectMapper objectMapper;

    @Value("${gemini.api.key:}")
    private String apiKey;

    @Value("${gemini.api.url:}")
    private String apiUrl;

    // AI 메시지 전송 및 DB 저장
    @Override
    @Transactional
    public EvAiChatResponseDTO sendMessage(Long memberId, String message) {
        log.info("@# EvAiChatServiceImpl.sendMessage()");
        log.info("@# memberId => {}", memberId);
        log.info("@# message => {}", message);

        EvAiChatRoomDTO roomDTO = getOrCreateRoom(memberId);
        Long roomId = roomDTO.getRoomId();

        saveMessage(roomId, "USER", message);
        List<EvAiChatMessageDTO> recentMessages = getRecentMessages(roomId);

        String answer;

        // 내 위치 질문은 Gemini가 임의 지역을 말하지 않도록 DB 기본 출발지만 사용한다.
        if (isDefaultLocationQuestion(message)) {
            Map<String, Object> location = evAiChatDAO.findDefaultLocationForAi(memberId);
            answer = buildDefaultLocationAnswer(location);
            saveMessage(roomId, "AI", answer);

            EvAiChatResponseDTO responseDTO = new EvAiChatResponseDTO(answer, "DEFAULT_LOCATION");
            responseDTO.setLocation(location);
            return responseDTO;
        }

        // 내 차량 질문은 Gemini가 임의 답변하지 않도록 DB 대표차량만 사용한다.
        if (isDefaultVehicleQuestion(message)) {
            Map<String, Object> vehicle = evAiChatDAO.findDefaultVehicleForAi(memberId);
            answer = buildDefaultVehicleAnswer(vehicle);
            saveMessage(roomId, "AI", answer);

            EvAiChatResponseDTO responseDTO = new EvAiChatResponseDTO(answer, "DEFAULT_VEHICLE");
            if (vehicle == null || vehicle.isEmpty()) {
                responseDTO.setActionType("VEHICLE_REGISTER");
                responseDTO.setButtonText("차량 등록하러 가기");
                responseDTO.setActionUrl("/vehicles/register");
            }
            return responseDTO;
        }

        // 내 예약 조회 질문은 예약 후보 추천이 아니라 실제 예약 내역을 조회한다.
        if (isMyReservationQuestion(message)) {
            List<Map<String, Object>> reservationList = evAiChatDAO.findMyReservationsForAi(memberId, 10);
            answer = buildMyReservationAnswer(reservationList);
            saveMessage(roomId, "AI", answer);

            EvAiChatResponseDTO responseDTO = new EvAiChatResponseDTO(answer, "MY_RESERVATION_LIST");
            responseDTO.setReservations(reservationList);
            return responseDTO;
        }

        // 예약 취소/변경/조회 방법 질문은 충전소 후보 추천으로 보내지 않고 RAG 안내로 처리한다.
        if (isReservationGuideQuestion(message)) {
            String ragContext = buildRagContext(memberId, message);
            answer = callGeminiWithContext(recentMessages, message, ragContext);
            saveMessage(roomId, "AI", answer);
            return new EvAiChatResponseDTO(answer, "RESERVATION_GUIDE");
        }

        EvAiChatIntentDTO intentDTO = analyzeIntent(message);
        fillSocFromRecentMessages(intentDTO, recentMessages);
        log.info("@# intent => {}, priority => {}", intentDTO.getIntent(), intentDTO.getPriority());

        switch (intentDTO.getIntent()) {
            case "STATION_RECOMMEND" -> {
                List<EvAiStationRecommendDTO> stationList = evAiChatDAO.findRecommendStations(memberId);
                answer = buildStationRecommendAnswer(memberId, stationList, intentDTO);
            }
            case "CHARGE_TIME", "CHARGE_COST" -> {
                answer = buildChargeAnswer(memberId, intentDTO);
            }
            default -> {
                String ragContext = buildRagContext(memberId, message);
                answer = callGeminiWithContext(recentMessages, message, ragContext);
            }
        }

        saveMessage(roomId, "AI", answer);
        EvAiChatResponseDTO responseDTO = new EvAiChatResponseDTO(answer);
        applyVehicleRegisterActionIfNeeded(answer, responseDTO);
        return responseDTO;
    }

    // 이전 채팅 메시지 조회
    @Override
    public List<EvAiChatMessageDTO> getChatHistory(Long memberId) {
        log.info("@# EvAiChatServiceImpl.getChatHistory()");
        log.info("@# memberId => {}", memberId);

        EvAiChatRoomDTO roomDTO = evAiChatDAO.findRoomByMemberId(memberId);
        if (roomDTO == null) {
            log.info("@# chat room empty");
            return List.of();
        }

        return getRecentMessages(roomDTO.getRoomId());
    }

    @Override
    public void clearChatCache(Long memberId) {
        log.info("@# EvAiChatServiceImpl.clearChatCache()");
        log.info("@# memberId => {}", memberId);

        EvAiChatRoomDTO room = evAiChatDAO.findRoomByMemberId(memberId);
        if (room == null) {
            stringRedisTemplate.delete("ai:reservation:candidate:" + memberId);
            log.info("@# AI room empty. reservation candidate cache only deleted => memberId: {}", memberId);
            return;
        }

        deleteAiRedisCache(memberId, room.getRoomId());
    }

    @Override
    @Transactional
    public void clearChatMessages(Long memberId) {
        log.info("@# EvAiChatServiceImpl.clearChatMessages()");
        log.info("@# memberId => {}", memberId);

        EvAiChatRoomDTO room = evAiChatDAO.findRoomByMemberId(memberId);
        if (room == null) {
            stringRedisTemplate.delete("ai:reservation:candidate:" + memberId);
            log.info("@# AI room empty. reservation candidate cache only deleted => memberId: {}", memberId);
            return;
        }

        int deletedCount = evAiChatDAO.deleteMessagesByRoomId(room.getRoomId());
        deleteAiRedisCache(memberId, room.getRoomId());

        log.info("@# AI chat messages deleted => memberId: {}, roomId: {}, count: {}",
                memberId, room.getRoomId(), deletedCount);
    }

    private void deleteAiRedisCache(Long memberId, Long roomId) {
        log.info("@# EvAiChatServiceImpl.deleteAiRedisCache()");

        String recentCacheKey = getChatCacheKey(roomId);
        String legacyCacheKey = "ai:chat:room:" + roomId;
        String reservationCandidateKey = "ai:reservation:candidate:" + memberId;

        stringRedisTemplate.delete(recentCacheKey);
        stringRedisTemplate.delete(legacyCacheKey);
        stringRedisTemplate.delete(reservationCandidateKey);

        log.info("@# AI Redis Cache Deleted => {}, {}, {}", recentCacheKey, legacyCacheKey, reservationCandidateKey);
    }

    private EvAiChatIntentDTO analyzeIntent(String message) {
        log.info("@# EvAiChatServiceImpl.analyzeIntent()");

        EvAiChatIntentDTO intentDTO = new EvAiChatIntentDTO();
        String value = message == null ? "" : message.replace(" ", "").toLowerCase();

        if (value.contains("충전소") || value.contains("근처") || value.contains("주변") || value.contains("추천") || value.contains("찾아")) {
            intentDTO.setIntent("STATION_RECOMMEND");
        } else if (value.contains("시간") || value.contains("얼마나걸") || value.contains("몇분")) {
            intentDTO.setIntent("CHARGE_TIME");
        } else if (value.contains("비용") || value.contains("요금") || value.contains("얼마") || value.contains("가격")) {
            intentDTO.setIntent("CHARGE_COST");
        } else {
            intentDTO.setIntent("GENERAL");
        }

        if (value.contains("저렴") || value.contains("싼") || value.contains("가격") || value.contains("요금") || value.contains("비용")) {
            intentDTO.setPriority("COST");
        } else if (value.contains("빠른") || value.contains("급속") || value.contains("초급속") || value.contains("속도")) {
            intentDTO.setPriority("SPEED");
        } else if (value.contains("가까") || value.contains("근처") || value.contains("주변")) {
            intentDTO.setPriority("DISTANCE");
        } else {
            intentDTO.setPriority("NONE");
        }

        Integer[] socValues = extractSocValues(message);
        if (socValues != null) {
            intentDTO.setCurrentSoc(socValues[0]);
            intentDTO.setTargetSoc(socValues[1]);
        }

        return intentDTO;
    }

    private String buildDefaultLocationAnswer(Map<String, Object> location) {
        log.info("@# EvAiChatServiceImpl.buildDefaultLocationAnswer()");

        if (location == null || location.isEmpty()) {
            return "현재 DB에 기본 출발지가 설정되어 있지 않습니다.\n충전소 찾기 화면에서 출발지를 등록하고 기본 출발지로 설정하면, AI가 그 위치 기준으로 주변 충전소를 추천할 수 있습니다.";
        }

        return "현재 AI가 사용하는 내 위치는 DB에 저장된 기본 출발지입니다.\n"
                + "위치명 : " + text(value(location, "locationName", "location_name")) + "\n"
                + "주소 : " + text(value(location, "address")) + "\n\n"
                + "이 위치를 기준으로 가까운 충전소, 저렴한 충전소, 빠른 충전기를 추천합니다.\n"
                + "아래 지도 버튼을 누르면 충전소 찾기 화면에서 이 위치를 바로 확인할 수 있습니다.";
    }

    private String buildDefaultVehicleAnswer(Map<String, Object> vehicle) {
        log.info("@# EvAiChatServiceImpl.buildDefaultVehicleAnswer()");

        if (vehicle == null || vehicle.isEmpty()) {
            return buildDefaultVehicleRequiredAnswer();
        }

        String nickname = text(value(vehicle, "vehicleNickname", "vehicle_nickname"));
        String vehicleName = text(value(vehicle, "manufacturer")) + " " + text(value(vehicle, "modelName", "model_name"));

        StringBuilder builder = new StringBuilder();
        builder.append("현재 대표차량은 ");
        if (!nickname.isBlank()) {
            builder.append("“").append(nickname).append("”으로 등록된 ");
        }
        builder.append(vehicleName.trim()).append("입니다.\n");
        builder.append("배터리 용량은 ").append(text(value(vehicle, "batteryCapacityKwh", "battery_capacity_kwh"))).append("kWh, ");
        builder.append("커넥터 타입은 ").append(text(value(vehicle, "connectorType", "connector_type"))).append(", ");
        builder.append("최대 충전 속도는 ").append(text(value(vehicle, "maxChargingSpeedKw", "max_charging_speed_kw"))).append("kW입니다.\n");
        builder.append("AI 충전소 추천과 충전 시간/비용 계산은 이 대표차량 기준으로 진행합니다.");

        return builder.toString();
    }

    private String buildDefaultVehicleRequiredAnswer() {
        return "아직 대표차량이 등록되어 있지 않습니다.\n"
                + "충전 시간과 비용 계산, 커넥터 타입에 맞는 충전소 추천을 위해 차량을 먼저 등록해 주세요.\n"
                + "아래 버튼을 누르면 차량 등록 화면으로 이동할 수 있습니다.";
    }

    private String buildStationRecommendAnswer(Long memberId,
                                               List<EvAiStationRecommendDTO> stationList,
                                               EvAiChatIntentDTO intentDTO) {
        log.info("@# EvAiChatServiceImpl.buildStationRecommendAnswer()");

        Map<String, Object> vehicle = evAiChatDAO.findDefaultVehicleForAi(memberId);
        if (vehicle == null || vehicle.isEmpty()) {
            return buildDefaultVehicleRequiredAnswer();
        }

        Map<String, Object> location = evAiChatDAO.findDefaultLocationForAi(memberId);

        if (location == null || location.isEmpty()) {
            return "충전소를 추천하려면 기본 출발지가 필요합니다.\n충전소 찾기 화면에서 출발지를 등록하고 기본 출발지로 설정해주세요.";
        }

        if (stationList == null || stationList.isEmpty()) {
            return "기본 출발지 기준으로 대표 차량과 맞는 사용 가능 충전기를 찾지 못했습니다.\n차량 커넥터 타입, 기본 출발지, 충전소 데이터를 확인해주세요.";
        }

        List<EvAiStationRecommendDTO> sortedList = new ArrayList<>(stationList);
        if ("COST".equals(intentDTO.getPriority())) {
            sortedList.sort((a, b) -> Double.compare(nullToZero(a.getPricePerKwh()), nullToZero(b.getPricePerKwh())));
        } else if ("SPEED".equals(intentDTO.getPriority())) {
            sortedList.sort((a, b) -> Double.compare(nullToZero(b.getChargingSpeedKw()), nullToZero(a.getChargingSpeedKw())));
        } else {
            sortedList.sort((a, b) -> Double.compare(nullToZero(a.getDistanceKm()), nullToZero(b.getDistanceKm())));
        }

        EvAiStationRecommendDTO first = sortedList.get(0);
        StringBuilder builder = new StringBuilder();
        builder.append("DB에 저장된 기본 출발지 기준으로 안내드릴게요.\n");
        builder.append("기본 출발지 : ").append(text(value(location, "locationName", "location_name")))
                .append(" / ").append(text(value(location, "address"))).append("\n");
        builder.append("대표 차량 : ").append(first.getManufacturer()).append(" ").append(first.getModelName())
                .append("(").append(first.getVehicleNickname()).append(")\n");
        builder.append("차량 커넥터 : ").append(first.getVehicleConnectorType()).append("\n");

        if ("COST".equals(intentDTO.getPriority())) {
            builder.append("정렬 기준 : 요금이 저렴한 순\n");
        } else if ("SPEED".equals(intentDTO.getPriority())) {
            builder.append("정렬 기준 : 충전 속도가 빠른 순\n");
        } else {
            builder.append("정렬 기준 : 가까운 순\n");
        }

        int count = Math.min(3, sortedList.size());
        for (int i = 0; i < count; i++) {
            EvAiStationRecommendDTO station = sortedList.get(i);
            builder.append("\n").append(i + 1).append(". ").append(station.getStationName());
            if (i == 0) {
                builder.append(" (가장 추천)");
            }
            builder.append("\n거리 : ").append(station.getDistanceKm()).append("km")
                    .append("\n충전기 : ").append(station.getChargerName())
                    .append("\n커넥터 : ").append(station.getConnectorType())
                    .append("\n출력 : ").append(station.getChargingSpeedKw()).append("kW")
                    .append("\n요금 : ").append(station.getPricePerKwh()).append("원/kWh")
                    .append("\n주소 : ").append(station.getAddress()).append("\n");
        }

        builder.append("\n예약까지 진행하려면 왼쪽 AI 예약 조건을 확인한 뒤 '오늘 예약 가능한 충전소 찾아줘'처럼 요청해주세요.");
        return builder.toString();
    }

    private String buildChargeAnswer(Long memberId, EvAiChatIntentDTO intentDTO) {
        log.info("@# EvAiChatServiceImpl.buildChargeAnswer()");

        Map<String, Object> vehicle = evAiChatDAO.findDefaultVehicleForAi(memberId);
        if (vehicle == null || vehicle.isEmpty()) {
            return buildDefaultVehicleRequiredAnswer();
        }

        if (intentDTO.getCurrentSoc() == null || intentDTO.getTargetSoc() == null) {
            return "현재 배터리 잔량과 목표 충전량을 %로 알려주세요.\n예) 30%에서 80%까지 충전하면 얼마나 걸려?";
        }

        EvAiChargeInfoDTO chargeInfo = calculateChargeInfo(memberId, intentDTO.getCurrentSoc(), intentDTO.getTargetSoc());
        if (chargeInfo == null) {
            return "충전 시간/비용 계산에 필요한 정보를 찾지 못했습니다.\n대표 차량, 기본 출발지, 사용 가능한 충전기 정보를 확인해주세요.";
        }

        return "고객님의 " + chargeInfo.getManufacturer() + " " + chargeInfo.getModelName() + "(" + chargeInfo.getVehicleNickname() + ") 기준으로 안내드립니다.\n"
                + "차량 커넥터 타입 : " + chargeInfo.getVehicleConnectorType() + "\n"
                + "충전기 커넥터 타입 : " + chargeInfo.getChargerConnectorType() + "\n"
                + "충전소 : " + chargeInfo.getStationName() + "\n"
                + "충전기 : " + chargeInfo.getChargerName() + "\n"
                + "충전기 유형 : " + chargeInfo.getChargerType() + "\n"
                + "충전 속도 : " + chargeInfo.getChargingSpeedKw() + "kW\n"
                + chargeInfo.getCurrentSoc() + "% → " + chargeInfo.getTargetSoc() + "%\n"
                + "필요 충전량 : " + round(chargeInfo.getRequiredKwh()) + "kWh\n"
                + "예상 충전 시간 : " + chargeInfo.getEstimatedMinutes() + "분\n"
                + "예상 충전 비용 : " + chargeInfo.getEstimatedCost() + "원\n"
                + "위 계산은 예상값이며 실제 충전 환경에 따라 달라질 수 있습니다.";
    }

    private EvAiChargeInfoDTO calculateChargeInfo(Long memberId, Integer currentSoc, Integer targetSoc) {
        log.info("@# EvAiChatServiceImpl.calculateChargeInfo()");

        if (currentSoc == null || targetSoc == null || targetSoc <= currentSoc) {
            return null;
        }

        EvAiChargeInfoDTO chargeInfo = evAiChatDAO.findChargeCalculationInfo(memberId);
        if (chargeInfo == null) {
            return null;
        }

        chargeInfo.setCurrentSoc(currentSoc);
        chargeInfo.setTargetSoc(targetSoc);

        double requiredKwh = chargeInfo.getBatteryCapacityKwh() * (targetSoc - currentSoc) / 100.0;
        requiredKwh = Math.round(requiredKwh * 100.0) / 100.0;
        chargeInfo.setRequiredKwh(requiredKwh);

        int estimatedMinutes = (int) Math.ceil((requiredKwh / chargeInfo.getChargingSpeedKw()) * 60 * CHARGING_EFFICIENCY);
        chargeInfo.setEstimatedMinutes(estimatedMinutes);

        int estimatedCost = (int) Math.round(requiredKwh * chargeInfo.getPricePerKwh());
        chargeInfo.setEstimatedCost(estimatedCost);

        return chargeInfo;
    }

    private String buildMyReservationAnswer(List<Map<String, Object>> reservationList) {
        log.info("@# EvAiChatServiceImpl.buildMyReservationAnswer()");

        if (reservationList == null || reservationList.isEmpty()) {
            return "현재 조회되는 예약 내역이 없습니다.\n충전소 찾기 또는 AI 예약 후보에서 새로운 예약을 진행할 수 있습니다.";
        }

        StringBuilder builder = new StringBuilder();
        builder.append("DB에 저장된 내 예약 내역을 조회했습니다.\n");
        builder.append("최근 예약 ").append(reservationList.size()).append("건을 보여드립니다.\n");

        int count = Math.min(5, reservationList.size());
        for (int i = 0; i < count; i++) {
            Map<String, Object> reservation = reservationList.get(i);
            builder.append("\n").append(i + 1).append(". ")
                    .append(text(value(reservation, "stationName", "station_name"))).append("\n")
                    .append("예약시간 : ").append(text(value(reservation, "startTime", "start_time"))).append(" ~ ")
                    .append(text(value(reservation, "endTime", "end_time"))).append("\n")
                    .append("충전기 : ").append(text(value(reservation, "chargerName", "charger_name"))).append("\n")
                    .append("상태 : ").append(text(value(reservation, "status"))).append("\n");
        }

        builder.append("\n각 예약 카드의 지도 버튼을 누르면 해당 충전소 위치를 지도에서 볼 수 있습니다.");
        return builder.toString();
    }

    private String buildRagContext(Long memberId, String message) {
        log.info("@# EvAiChatServiceImpl.buildRagContext()");

        String keyword = extractRagKeyword(message);
        StringBuilder builder = new StringBuilder();

        try {
            List<Map<String, Object>> faqList = evAiChatDAO.findAiRagFaqs(keyword, 5);
            if (faqList != null && !faqList.isEmpty()) {
                builder.append("FAQ 데이터\n");
                for (Map<String, Object> faq : faqList) {
                    builder.append("질문: ").append(text(value(faq, "question"))).append("\n")
                            .append("답변: ").append(text(value(faq, "answer"))).append("\n\n");
                }
            }
        } catch (Exception e) {
            log.warn("@# faq rag search fail => {}", e.getMessage());
        }

        try {
            List<Map<String, Object>> noticeList = evAiChatDAO.findAiRagNotices(keyword, 3);
            if (noticeList != null && !noticeList.isEmpty()) {
                builder.append("공지사항 데이터\n");
                for (Map<String, Object> notice : noticeList) {
                    builder.append("제목: ").append(text(value(notice, "title"))).append("\n")
                            .append("내용: ").append(limitText(text(value(notice, "content")), 180)).append("\n\n");
                }
            }
        } catch (Exception e) {
            log.warn("@# notice rag search fail => {}", e.getMessage());
        }

        try {
            List<Map<String, Object>> complaintList = evAiChatDAO.findAiRagComplaints(memberId, keyword, 3);
            if (complaintList != null && !complaintList.isEmpty()) {
                builder.append("내 민원 데이터\n");
                for (Map<String, Object> complaint : complaintList) {
                    builder.append("민원: ").append(text(value(complaint, "title"))).append("\n")
                            .append("유형: ").append(text(value(complaint, "complaintType", "complaint_type"))).append("\n")
                            .append("상태: ").append(text(value(complaint, "status"))).append("\n")
                            .append("관리자 메모: ").append(text(value(complaint, "adminMemo", "admin_memo"))).append("\n\n");
                }
            }
        } catch (Exception e) {
            log.warn("@# complaint rag search fail => {}", e.getMessage());
        }

        return builder.toString();
    }

    private void applyVehicleRegisterActionIfNeeded(String answer, EvAiChatResponseDTO responseDTO) {
        if (answer == null || responseDTO == null) {
            return;
        }

        if (answer.contains("대표차량이 등록되어 있지 않습니다") || answer.contains("대표차량이 필요합니다")) {
            responseDTO.setActionType("VEHICLE_REGISTER");
            responseDTO.setButtonText("차량 등록하러 가기");
            responseDTO.setActionUrl("/vehicles/register");
        }
    }

    private String callGeminiWithContext(List<EvAiChatMessageDTO> recentMessages, String message, String ragContext) {
        log.info("@# EvAiChatServiceImpl.callGeminiWithContext()");

        StringBuilder promptBuilder = new StringBuilder();
        promptBuilder.append("너는 공공 전기차 충전 인프라 운영 MIS 플랫폼의 AI 충전 비서다.\n");
        promptBuilder.append("답변은 짧고 친절하게 작성하고, 모르는 데이터는 지어내지 마라.\n");
        promptBuilder.append("기본 출발지, 차량, 충전소, FAQ, 공지사항, 민원 데이터가 있으면 반드시 그 데이터를 기준으로 답변해라.\n\n");

        promptBuilder.append("이전 대화\n");
        for (EvAiChatMessageDTO dto : recentMessages) {
            promptBuilder.append(dto.getSenderType()).append(" : ").append(dto.getMessage()).append("\n");
        }

        if (ragContext != null && !ragContext.isBlank()) {
            promptBuilder.append("\nRAG 조회 데이터\n").append(ragContext).append("\n");
        }

        promptBuilder.append("현재 질문\n").append(message);

        String geminiAnswer = callGeminiApi(promptBuilder.toString());
        if (geminiAnswer == null || geminiAnswer.isBlank()) {
            if (ragContext != null && !ragContext.isBlank()) {
                return "조회된 FAQ, 공지사항, 민원 데이터를 확인했습니다.\n" + limitText(ragContext, 500);
            }
            return "질문을 확인했습니다. 충전소 추천, 예약 가능 충전기 조회, 충전 시간/비용 계산, 민원/FAQ 안내를 도와드릴 수 있습니다.";
        }

        return geminiAnswer;
    }

    private String callGeminiApi(String prompt) {
        log.info("@# EvAiChatServiceImpl.callGeminiApi()");

        if (apiUrl == null || apiUrl.isBlank() || apiKey == null || apiKey.isBlank()) {
            log.warn("@# Gemini API config empty");
            return "";
        }

        try {
            String url = apiUrl + "?key=" + apiKey;
            RestTemplate restTemplate = new RestTemplate();

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            Map<String, Object> body = new HashMap<>();
            body.put("contents", List.of(Map.of("parts", List.of(Map.of("text", prompt)))));

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);
            Map<String, Object> response = restTemplate.postForObject(url, request, Map.class);

            if (response == null) {
                return "";
            }

            List<Map<String, Object>> candidates = (List<Map<String, Object>>) response.get("candidates");
            if (candidates == null || candidates.isEmpty()) {
                return "";
            }

            Map<String, Object> content = (Map<String, Object>) candidates.get(0).get("content");
            if (content == null) {
                return "";
            }

            List<Map<String, Object>> parts = (List<Map<String, Object>>) content.get("parts");
            if (parts == null || parts.isEmpty()) {
                return "";
            }

            return String.valueOf(parts.get(0).get("text"));
        } catch (Exception e) {
            log.warn("@# Gemini API call fail => {}", e.getMessage());
            return "";
        }
    }

    private EvAiChatRoomDTO getOrCreateRoom(Long memberId) {
        log.info("@# EvAiChatServiceImpl.getOrCreateRoom()");

        EvAiChatRoomDTO roomDTO = evAiChatDAO.findRoomByMemberId(memberId);
        if (roomDTO != null) {
            return roomDTO;
        }

        roomDTO = new EvAiChatRoomDTO();
        roomDTO.setMemberId(memberId);
        roomDTO.setTitle("AI 채팅");
        evAiChatDAO.insertRoom(roomDTO);

        log.info("@# created roomId => {}", roomDTO.getRoomId());
        return roomDTO;
    }

    private void saveMessage(Long roomId, String senderType, String message) {
        log.info("@# EvAiChatServiceImpl.saveMessage()");
        log.info("@# senderType => {}", senderType);

        EvAiChatMessageDTO messageDTO = new EvAiChatMessageDTO();
        messageDTO.setRoomId(roomId);
        messageDTO.setSenderType(senderType);
        messageDTO.setMessage(message);

        evAiChatDAO.insertMessage(messageDTO);
        addMessageToCache(roomId, messageDTO);
    }

    private List<EvAiChatMessageDTO> getRecentMessages(Long roomId) {
        String key = getChatCacheKey(roomId);

        try {
            List<String> cachedList = stringRedisTemplate.opsForList().range(key, 0, -1);
            if (cachedList != null && !cachedList.isEmpty()) {
                log.info("@# ai chat cache hit => roomId: {}", roomId);

                List<EvAiChatMessageDTO> messageList = new ArrayList<>();
                for (String cached : cachedList) {
                    messageList.add(objectMapper.readValue(cached, EvAiChatMessageDTO.class));
                }

                return messageList;
            }
        } catch (Exception e) {
            log.warn("@# ai chat cache read fail => {}", e.getMessage());
        }

        List<EvAiChatMessageDTO> dbList = evAiChatDAO.findRecentMessagesByRoomId(roomId);
        saveRecentMessagesToCache(roomId, dbList);
        return dbList;
    }

    private void saveRecentMessagesToCache(Long roomId, List<EvAiChatMessageDTO> messageList) {
        if (messageList == null || messageList.isEmpty()) {
            return;
        }

        String key = getChatCacheKey(roomId);

        try {
            stringRedisTemplate.delete(key);
            for (EvAiChatMessageDTO dto : messageList) {
                stringRedisTemplate.opsForList().rightPush(key, objectMapper.writeValueAsString(dto));
            }
            stringRedisTemplate.opsForList().trim(key, -CHAT_CACHE_LIMIT, -1);
            stringRedisTemplate.expire(key, CHAT_CACHE_TTL);
        } catch (Exception e) {
            log.warn("@# ai chat cache save fail => {}", e.getMessage());
        }
    }

    private void addMessageToCache(Long roomId, EvAiChatMessageDTO messageDTO) {
        String key = getChatCacheKey(roomId);

        try {
            stringRedisTemplate.opsForList().rightPush(key, objectMapper.writeValueAsString(messageDTO));
            stringRedisTemplate.opsForList().trim(key, -CHAT_CACHE_LIMIT, -1);
            stringRedisTemplate.expire(key, CHAT_CACHE_TTL);
        } catch (Exception e) {
            log.warn("@# ai chat cache append fail => {}", e.getMessage());
        }
    }

    private String getChatCacheKey(Long roomId) {
        return "ai:chat:recent:" + roomId;
    }

    private boolean isDefaultLocationQuestion(String message) {
        if (message == null) {
            return false;
        }

        String value = message.replace(" ", "").toLowerCase();
        boolean locationWord = value.contains("내위치") || value.contains("기본출발지") || value.contains("출발지") || value.contains("위치어디");
        boolean actionWord = value.contains("충전소") || value.contains("추천") || value.contains("찾") || value.contains("예약");
        return locationWord && !actionWord;
    }

    private boolean isDefaultVehicleQuestion(String message) {
        if (message == null) {
            return false;
        }

        String value = message.replace(" ", "").toLowerCase();
        boolean vehicleWord = value.contains("내차")
                || value.contains("내차량")
                || value.contains("대표차량")
                || value.contains("기본차량")
                || value.contains("등록차량")
                || value.contains("차량뭐")
                || value.contains("차뭐");
        boolean actionWord = value.contains("충전소") || value.contains("예약") || value.contains("추천") || value.contains("찾");
        return vehicleWord && !actionWord;
    }

    private boolean isMyReservationQuestion(String message) {
        if (message == null) {
            return false;
        }

        String value = message.replace(" ", "").toLowerCase();
        boolean reservationWord = value.contains("내예약") || value.contains("예약내역") || value.contains("예약목록") || value.contains("예약조회") || value.contains("예약한곳") || value.contains("내가예약");
        boolean cancelGuide = value.contains("취소") || value.contains("변경") || value.contains("방법") || value.contains("어떻게");
        return reservationWord && !cancelGuide;
    }

    private boolean isReservationGuideQuestion(String message) {
        if (message == null) {
            return false;
        }

        String value = message.replace(" ", "").toLowerCase();
        boolean reservationWord = value.contains("예약");
        boolean guideWord = value.contains("취소") || value.contains("변경") || value.contains("방법") || value.contains("어떻게") || value.contains("조회");
        boolean recommendWord = value.contains("가능") || value.contains("후보") || value.contains("추천") || value.contains("찾") || value.contains("해줘");
        return reservationWord && guideWord && !recommendWord;
    }

    private void fillSocFromRecentMessages(EvAiChatIntentDTO intentDTO, List<EvAiChatMessageDTO> recentMessages) {
        if (intentDTO.getCurrentSoc() != null && intentDTO.getTargetSoc() != null) {
            return;
        }

        if (recentMessages == null || recentMessages.isEmpty()) {
            return;
        }

        for (int i = recentMessages.size() - 1; i >= 0; i--) {
            EvAiChatMessageDTO messageDTO = recentMessages.get(i);
            if (!"USER".equals(messageDTO.getSenderType())) {
                continue;
            }

            Integer[] socValues = extractSocValues(messageDTO.getMessage());
            if (socValues == null) {
                continue;
            }

            intentDTO.setCurrentSoc(socValues[0]);
            intentDTO.setTargetSoc(socValues[1]);
            return;
        }
    }

    private Integer[] extractSocValues(String message) {
        if (message == null || message.trim().isEmpty()) {
            return null;
        }

        Pattern pattern = Pattern.compile("(\\d{1,3})\\s*%");
        Matcher matcher = pattern.matcher(message);
        List<Integer> socList = new ArrayList<>();

        while (matcher.find()) {
            int soc = Integer.parseInt(matcher.group(1));
            if (soc >= 0 && soc <= 100) {
                socList.add(soc);
            }
        }

        if (socList.size() < 2) {
            return null;
        }

        int currentSoc = socList.get(0);
        int targetSoc = socList.get(1);
        if (targetSoc <= currentSoc) {
            return null;
        }

        return new Integer[] { currentSoc, targetSoc };
    }

    private String extractRagKeyword(String message) {
        if (message == null) {
            return "";
        }

        String value = message.replace(" ", "");
        if (value.contains("결제")) {
            return "결제";
        }
        if (value.contains("취소")) {
            return "예약";
        }
        if (value.contains("장애") || value.contains("고장")) {
            return "장애";
        }
        if (value.contains("민원")) {
            return "민원";
        }
        if (value.contains("공지")) {
            return "";
        }
        if (value.contains("예약")) {
            return "예약";
        }
        if (value.contains("FAQ") || value.contains("faq") || value.contains("자주")) {
            return "";
        }

        return message.length() > 20 ? "" : message.trim();
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

    private String text(Object value) {
        return value == null ? "" : String.valueOf(value).trim();
    }

    private double nullToZero(Double value) {
        return value == null ? 0.0 : value;
    }

    private double round(Double value) {
        if (value == null) {
            return 0.0;
        }
        return Math.round(value * 100.0) / 100.0;
    }

    private String limitText(String value, int limit) {
        if (value == null) {
            return "";
        }
        if (value.length() <= limit) {
            return value;
        }
        return value.substring(0, limit) + "...";
    }
}
