package com.ev.service.user;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.time.Duration;
import java.util.ArrayList;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;

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

    private final EvAiChatDAO evAiChatDAO;
    private static final int CHAT_CACHE_LIMIT = 20;
    private static final Duration CHAT_CACHE_TTL = Duration.ofHours(24);

    private final StringRedisTemplate stringRedisTemplate;
    private final ObjectMapper objectMapper;

    // Gemini API Key
    @Value("${gemini.api.key}")
    private String apiKey;

    // Gemini API URL
    @Value("${gemini.api.url}")
    private String apiUrl;
    
    // 충전 시간 계산 보정 계수
    private static final double CHARGING_EFFICIENCY = 1.15;


 // AI 메시지 전송 및 DB 저장
    @Override
    @Transactional
    public EvAiChatResponseDTO sendMessage(Long memberId, String message) {

        log.info("@# EvAiChatServiceImpl.sendMessage()");
        log.info("@# memberId => {}", memberId);
        log.info("@# message => {}", message);

        // 채팅방 조회 또는 생성
        EvAiChatRoomDTO roomDTO = getOrCreateRoom(memberId);

        Long roomId = roomDTO.getRoomId();

        log.info("@# roomId => {}", roomId);

        // 사용자 메시지 저장
        saveMessage(roomId, "USER", message);

        // 최근 대화 조회
        List<EvAiChatMessageDTO> recentMessages =
                getRecentMessages(roomId);

        // 사용자 메시지 의도 분석
        EvAiChatIntentDTO intentDTO = analyzeIntent(message);
        
        // 이전 대화 SOC 보정
        fillSocFromRecentMessages(intentDTO, recentMessages);

        log.info("@# intent => {}", intentDTO.getIntent());

        // AI 대표 기능용 충전소 추천 목록
        List<EvAiStationRecommendDTO> stationList = List.of();
        // AI 대표기능용 충전시간/충전비용 정보
        EvAiChargeInfoDTO chargeInfo = null;
        
        String intent = intentDTO.getIntent();

        if (intent == null) {
            intent = "GENERAL";
        }
        
        String answer = null;
        
        // intent 기반 기능 분기
        switch (intent) {

            case "STATION_RECOMMEND" -> {
                log.info("@# recommend station search start");

                stationList = evAiChatDAO.findRecommendStations(memberId);

                log.info("@# recommend station count => {}", stationList.size());
            }
            case "CHARGE_TIME" -> {

                log.info("@# charge time calculation start");

                if (intentDTO.getCurrentSoc() == null
                        || intentDTO.getTargetSoc() == null) {

                	answer = """
                	        현재 배터리 잔량과 목표 충전량을 %로 알려주세요.

                	        예)
                	        30%에서 80%까지 충전시간 알려줘
                	        """;

                    saveMessage(roomId, "AI", answer);

                    return new EvAiChatResponseDTO(answer);
                }

                chargeInfo =
                        calculateChargeInfo(
                                memberId,
                                intentDTO.getCurrentSoc(),
                                intentDTO.getTargetSoc()
                        );
            }
            case "CHARGE_COST" -> {

                log.info("@# charge cost calculation start");

                if (intentDTO.getCurrentSoc() == null
                        || intentDTO.getTargetSoc() == null) {

                	answer = """
                	        현재 배터리 잔량과 목표 충전량을 %로 알려주세요.

                	        예)
                	        30%에서 80%까지 충전하면 얼마야?
                	        """;

                    saveMessage(roomId, "AI", answer);

                    return new EvAiChatResponseDTO(answer);
                }

                chargeInfo =
                        calculateChargeInfo(
                                memberId,
                                intentDTO.getCurrentSoc(),
                                intentDTO.getTargetSoc()
                        );
            }

            default -> {
                log.info("@# general ai chat");
            }
        }

        // Gemini 호출
        answer =
                callGemini(
                        recentMessages,
                        message,
                        stationList,
                        chargeInfo,
                        intentDTO
                );

        // AI 답변 저장
        saveMessage(roomId, "AI", answer);

        // AI 답변 반환
        return new EvAiChatResponseDTO(answer);
    }

 // 충전 시간 및 비용 계산
    private EvAiChargeInfoDTO calculateChargeInfo(
            Long memberId,
            Integer currentSoc,
            Integer targetSoc) {
    	
    	if (currentSoc == null || targetSoc == null) {
    	    log.warn("@# currentSoc or targetSoc is null");
    	    return null;
    	}
    	
    	
        log.info("@# calculateChargeInfo()");

        EvAiChargeInfoDTO chargeInfo =
                evAiChatDAO.findChargeCalculationInfo(memberId);

        if (chargeInfo == null) {
            return null;
        }

        chargeInfo.setCurrentSoc(currentSoc);
        chargeInfo.setTargetSoc(targetSoc);

        // 필요 충전량 계산
        double requiredKwh =
                chargeInfo.getBatteryCapacityKwh()
                * (targetSoc - currentSoc)
                / 100.0;

        chargeInfo.setRequiredKwh(requiredKwh);

        // 예상 충전 시간 계산
        int estimatedMinutes =
                (int) Math.round(
                        (requiredKwh
                        / chargeInfo.getChargingSpeedKw())
                        * 60
                        * CHARGING_EFFICIENCY
                );

        chargeInfo.setEstimatedMinutes(estimatedMinutes);

        // 예상 충전 비용 계산
        int estimatedCost =
                (int) Math.round(
                        requiredKwh
                        * chargeInfo.getPricePerKwh()
                );

        chargeInfo.setEstimatedCost(estimatedCost);

        return chargeInfo;
    }   
    
    
 // 사용자 메시지 의도 분석
    private EvAiChatIntentDTO analyzeIntent(String message) {

        log.info("@# analyzeIntent()");

        EvAiChatIntentDTO intentDTO = new EvAiChatIntentDTO();

        // 빈 메시지는 일반 질문으로 처리
        if (message == null || message.trim().isEmpty()) {
            intentDTO.setIntent("GENERAL");
            intentDTO.setPriority("NONE");
            return intentDTO;
        }

        // 의도 분석 프롬프트 생성
        String prompt = """
                너는 EV Charge 전기차 충전 예약 시스템의 의도 분석기다.

                사용자의 문장을 분석해서 반드시 JSON 형식으로만 답변해라.
                설명 문장, markdown, 코드블럭은 절대 사용하지 마라.

                가능한 intent:
                - STATION_RECOMMEND: 충전소 추천, 근처 충전소 찾기, 내 차량에 맞는 충전소 요청
                - CHARGE_TIME: 충전 시간 계산, 몇 분 걸리는지, 충전 완료 예상 시간
                - CHARGE_COST: 충전 비용 계산, 충전 요금, 얼마 나오는지
                - GENERAL: 일반 질문

                JSON 형식:
                {
                  "intent": "STATION_RECOMMEND",
                  "currentSoc": null,
                  "targetSoc": null,
                  "priority": "DISTANCE"
                }

                필드 설명:
                - intent: STATION_RECOMMEND, CHARGE_TIME, CHARGE_COST, GENERAL 중 하나
                - currentSoc: 현재 배터리 잔량 숫자, 없으면 null
                - targetSoc: 목표 배터리 잔량 숫자, 없으면 null
                - priority: DISTANCE, SPEED, COST, NONE 중 하나

                예시:
                사용자 문장: 근처 충전소 추천해줘
                결과: {"intent":"STATION_RECOMMEND","currentSoc":null,"targetSoc":null,"priority":"DISTANCE"}

                사용자 문장: 배터리 30%에서 80%까지 얼마나 걸려?
                결과: {"intent":"CHARGE_TIME","currentSoc":30,"targetSoc":80,"priority":"NONE"}

                사용자 문장: 20%에서 90%까지 충전하면 얼마야?
                결과: {"intent":"CHARGE_COST","currentSoc":20,"targetSoc":90,"priority":"COST"}

                사용자 문장:
                """ + message;

        try {
            // Gemini 의도 분석 호출
            String result = callGeminiApi(prompt);

            log.info("@# intent raw result => {}", result);

            // JSON 코드블럭 제거
            result = result
                    .replace("```json", "")
                    .replace("```", "")
                    .trim();

            ObjectMapper objectMapper = new ObjectMapper();

            // JSON 문자열을 DTO로 변환
            intentDTO = objectMapper.readValue(
                    result,
                    EvAiChatIntentDTO.class
            );

            // 기본값 보정
            if (intentDTO.getIntent() == null) {
                intentDTO.setIntent("GENERAL");
            }

            if (intentDTO.getPriority() == null) {
                intentDTO.setPriority("NONE");
            }

        } catch (Exception e) {
            log.error("@# analyzeIntent error", e);

            intentDTO.setIntent("GENERAL");
            intentDTO.setPriority("NONE");
        }

        return intentDTO;
    } 

 // 이전 대화에서 SOC 정보를 찾아 현재 intent에 보정
 // %가 붙은 숫자만 SOC 값으로 인정
 private void fillSocFromRecentMessages(
         EvAiChatIntentDTO intentDTO,
         List<EvAiChatMessageDTO> recentMessages) {

     if (intentDTO.getCurrentSoc() != null
             && intentDTO.getTargetSoc() != null) {
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

         Integer[] socValues =
                 extractSocValues(messageDTO.getMessage());

         if (socValues == null) {
             continue;
         }

         intentDTO.setCurrentSoc(socValues[0]);
         intentDTO.setTargetSoc(socValues[1]);

         log.info("@# previous soc applied => {} -> {}",
                 socValues[0],
                 socValues[1]);

         return;
     }
 }
 
	//메시지에서 30%, 80% 같은 SOC 값 추출
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
    
 // 이전 채팅 메시지 조회
    @Override
    public List<EvAiChatMessageDTO> getChatHistory(Long memberId) {

        log.info("@# EvAiChatServiceImpl.getChatHistory()");
        log.info("@# memberId => {}", memberId);

        // 회원의 채팅방 조회
        EvAiChatRoomDTO roomDTO =
                evAiChatDAO.findRoomByMemberId(memberId);

        // 채팅방이 없으면 빈 목록 반환
        if (roomDTO == null) {

            log.info("@# chat room empty");

            return List.of();
        }

        Long roomId = roomDTO.getRoomId();

        log.info("@# roomId => {}", roomId);

        // 채팅방 메시지 목록 조회
        List<EvAiChatMessageDTO> messageList =
                getRecentMessages(roomId);

        log.info("@# message count => {}", messageList.size());

        return messageList;
    }

    // 채팅방 조회 또는 생성
    private EvAiChatRoomDTO getOrCreateRoom(Long memberId) {

        log.info("@# EvAiChatServiceImpl.getOrCreateRoom()");

        // 기존 채팅방 조회
        EvAiChatRoomDTO roomDTO =
                evAiChatDAO.findRoomByMemberId(memberId);

        // 기존 채팅방이 있으면 반환
        if (roomDTO != null) {
            return roomDTO;
        }

        // 채팅방이 없으면 새로 생성
        roomDTO = new EvAiChatRoomDTO();

        roomDTO.setMemberId(memberId);
        roomDTO.setTitle("AI 채팅");

        evAiChatDAO.insertRoom(roomDTO);

        log.info("@# created roomId => {}", roomDTO.getRoomId());

        return roomDTO;
    }

 // 채팅 메시지 저장
    private void saveMessage(Long roomId, String senderType, String message) {

        log.info("@# EvAiChatServiceImpl.saveMessage()");
        log.info("@# senderType => {}", senderType);

        EvAiChatMessageDTO messageDTO = new EvAiChatMessageDTO();

        messageDTO.setRoomId(roomId);
        messageDTO.setSenderType(senderType);
        messageDTO.setMessage(message);

        // DB 메시지 저장
        evAiChatDAO.insertMessage(messageDTO);

        // Redis 최근 대화 캐시 저장
        addMessageToCache(roomId, messageDTO);
    }

 // AI 답변 프롬프트 생성
    private String callGemini(
            List<EvAiChatMessageDTO> recentMessages,
            String message,
            List<EvAiStationRecommendDTO> stationList,
            EvAiChargeInfoDTO chargeInfo,
            EvAiChatIntentDTO intentDTO) {

        log.info("@# EvAiChatServiceImpl.callGemini()");

        // AI 답변 프롬프트 생성
        StringBuilder promptBuilder = new StringBuilder();

        promptBuilder.append("""
                너는 EV Charge 전기차 충전 예약 시스템 AI 챗봇이다.

                답변 규칙:
                1. 사용자의 이전 대화를 참고하여 자연스럽게 이어서 답변해라.
                2. 답변은 웹 채팅 형식처럼 짧고 친절하게 작성해라.
                3. markdown 기호(**, ##, -, 1.)는 사용하지 마라.
                4. 충전소 찾기, 예약 방법, 차량 충전 타입,
                   충전 시간, 충전 비용 관련 질문을 도와줘라.
                5. 너무 긴 답변은 피하고 핵심만 설명해라.

                ===== 이전 대화 =====
                """);

        // 최근 대화 추가
        for (EvAiChatMessageDTO dto : recentMessages) {

            promptBuilder.append(dto.getSenderType())
                    .append(" : ")
                    .append(dto.getMessage())
                    .append("\n");
        }

        // 충전소 추천 결과 추가
        if (stationList != null && !stationList.isEmpty()) {

        	promptBuilder.append("""

        	        ===== DB 조회 결과: 대표 차량 기준 주변 충전소 =====
        	        아래 데이터는 사용자의 대표 차량과 기본 위치를 기준으로 조회한 실제 DB 결과다.
        	        이 데이터는 기본 위치 기준 가까운 충전소 후보 5개다.

        	        답변 규칙:
        	        1. 조회된 충전소 중 최대 3개까지만 안내해라.
        	        2. priority가 DISTANCE이면 거리(distanceKm)가 가장 가까운 충전소에 "(가장 추천)" 문구를 붙여라.
        	        3. priority가 SPEED이면 충전 속도(chargingSpeedKw)가 가장 높은 충전소에 "(가장 추천)" 문구를 붙여라.
        	        4. priority가 COST이면 충전기 요금(pricePerKwh)이 가장 저렴한 충전소에 "(가장 추천)" 문구를 붙여라.
        	        5. priority가 NONE이면 거리(distanceKm)가 가장 가까운 충전소에 "(가장 추천)" 문구를 붙여라.
        	        6. 차량 모델명과 차량 별칭을 함께 언급해라.
        	        7. 충전소명, 거리, 사용 가능한 충전기 정보를 함께 안내해라.
        	        8. 충전기 정보는 충전기명, 충전기 유형, 커넥터 타입, 충전 속도를 포함해라.
        	        9. 충전기 요금 정보를 함께 안내해라.
        	        10. 없는 충전소나 충전기 정보를 지어내지 마라.
        	        11. 답변은 짧고 보기 쉽게 작성해라.
        	        12. 사용자가 "가장 싼 곳", "저렴한 곳", "요금 싼 곳"을 요청했다면 priority가 COST인 것으로 보고 요금이 가장 낮은 충전소를 가장 추천해라.
        	        13. 사용자가 "빠른 곳", "급속", "빨리 충전"을 요청했다면 priority가 SPEED인 것으로 보고 충전 속도가 가장 높은 충전소를 가장 추천해라.
        	        14. 사용자가 "가까운 곳", "근처"를 요청했다면 priority가 DISTANCE인 것으로 보고 거리가 가장 가까운 충전소를 가장 추천해라.

        	        답변 형식:

        	        고객님의 대표 차량 [제조사] [모델명]([별칭]) 기준으로
        	        조건에 맞는 충전소를 안내해드릴게요.

        	        1. [충전소명] (가장 추천)
        	        거리 : x.xxkm
        	        사용 가능한 충전기
        	        - [충전기명]
        	          유형 : [충전기 유형]
        	          커넥터 : [커넥터 타입]
        	          출력 : [충전속도]kW
        	        충전기 요금 : xxx원/kWh

        	        2. [충전소명]
        	        거리 : x.xxkm
        	        사용 가능한 충전기
        	        - [충전기명]
        	          유형 : [충전기 유형]
        	          커넥터 : [커넥터 타입]
        	          출력 : [충전속도]kW
        	        충전기 요금 : xxx원/kWh

        	        3. [충전소명]
        	        거리 : x.xxkm
        	        사용 가능한 충전기
        	        - [충전기명]
        	          유형 : [충전기 유형]
        	          커넥터 : [커넥터 타입]
        	          출력 : [충전속도]kW
        	        충전기 요금 : xxx원/kWh

        	        현재 추천 우선순위:
        	        """ + intentDTO.getPriority() + "\n\n");
        	EvAiStationRecommendDTO firstStation = stationList.get(0);

        	promptBuilder.append("대표 차량: ")
            .append(firstStation.getManufacturer())
            .append(" ")
            .append(firstStation.getModelName())
            .append(" (")
            .append(firstStation.getVehicleNickname())
            .append(")")
            .append("\n");

        	promptBuilder.append("차량 커넥터 타입: ")
        	        .append(firstStation.getVehicleConnectorType())
        	        .append("\n\n");
        	
            for (EvAiStationRecommendDTO station : stationList) {
            	
            	promptBuilder.append("충전소명: ")
                        .append(station.getStationName())
                        .append("\n");

                promptBuilder.append("주소: ")
                        .append(station.getAddress())
                        .append("\n");

                promptBuilder.append("충전기명: ")
                        .append(station.getChargerName())
                        .append("\n");

                promptBuilder.append("충전기 유형: ")
                        .append(station.getChargerType())
                        .append("\n");

                promptBuilder.append("커넥터 타입: ")
                        .append(station.getConnectorType())
                        .append("\n");

                promptBuilder.append("충전 속도: ")
                        .append(station.getChargingSpeedKw())
                        .append("kW\n");

                promptBuilder.append("요금: ")
                        .append(station.getPricePerKwh())
                        .append("원/kWh\n");

                promptBuilder.append("상태: ")
                        .append(station.getStatus())
                        .append("\n");

                promptBuilder.append("거리: ")
                        .append(station.getDistanceKm())
                        .append("km\n\n");
            }

        } else if ("STATION_RECOMMEND".equals(intentDTO.getIntent())) {

            // 충전소 추천 결과 없음
            promptBuilder.append("""

                    ===== DB 조회 결과: 대표 차량 기준 주변 충전소 =====
                    조회된 충전소가 없다.
                    대표 차량 또는 기본 위치가 설정되지 않았을 수 있다.
                    사용자에게 대표 차량과 기본 위치 설정 여부를 확인하도록 안내해라.

                    """);
        }

        // 충전 시간/비용 계산 결과 추가
        if (chargeInfo != null) {

            promptBuilder.append("""

                    ===== 충전 시간/비용 계산 결과 =====
                    아래 데이터는 사용자의 대표 차량과 가장 가까운 사용가능 충전기를 기준으로 계산한 결과다.
                    사용자가 충전 시간 또는 충전 비용을 물어보면 반드시 이 데이터를 기준으로 답변해라.
                    계산 결과를 임의로 바꾸지 마라.

					답변 규칙:
					1. 사용자의 대표 차량 기준으로 계산했다고 안내해라.
					2. 차량 커넥터 타입과 충전기 커넥터 타입이 일치하여 충전 가능한 충전기라고 설명해라.
					3. 충전 시간 또는 비용 답변 시 반드시 아래 정보를 함께 안내해라.
					   - 차량 모델명
					   - 차량 별칭
					   - 차량 커넥터 타입
					   - 충전소명
					   - 충전기명
					   - 충전기 유형
					   - 충전기 커넥터 타입
					   - 충전속도(kW)
					4. 계산 결과는 예상값이며 실제 충전 환경에 따라 달라질 수 있다고 짧게 안내해라.
					5. 답변은 반드시 줄바꿈을 사용하여 항목별로 구분해라.
					6. 차량, 충전소, 충전기, 충전시간, 충전비용을 각각 별도 줄에 출력해라.
					7. 목록 형태(-)를 사용하지 말고 "항목 : 값" 형식으로 출력해라.
					8. 답변 형식을 임의로 변경하지 말고 반드시 위 형식을 그대로 사용해라.
            		
            		답변 형식:

					고객님의 [차량모델]([별칭]) 기준으로 안내드립니다.
					
					차량 커넥터 타입 : [차량 커넥터]
					충전기 커넥터 타입 : [충전기 커넥터]
					충전 가능
					
					충전소 : [충전소명]
					충전기 : [충전기명]
					충전기 유형 : [충전기 유형]
					충전 속도 : [충전속도]kW
					
					[현재SOC]% → [목표SOC]%
					
					예상 충전 시간 : [예상시간]분
					예상 충전 비용 : [예상비용]원
					
					위 계산은 예상값이며 실제 충전 환경에 따라 달라질 수 있습니다.
                    """);

            promptBuilder.append("제조사: ")
                    .append(chargeInfo.getManufacturer())
                    .append("\n");

            promptBuilder.append("차량 모델: ")
                    .append(chargeInfo.getModelName())
                    .append("\n");

            promptBuilder.append("차량 별칭: ")
                    .append(chargeInfo.getVehicleNickname())
                    .append("\n");

            promptBuilder.append("차량 커넥터 타입: ")
            .append(chargeInfo.getVehicleConnectorType())
            .append("\n");

            promptBuilder.append("충전기 커넥터 타입: ")
            .append(chargeInfo.getChargerConnectorType())
            .append("\n");

            promptBuilder.append("배터리 용량: ")
                    .append(chargeInfo.getBatteryCapacityKwh())
                    .append("kWh\n");

            promptBuilder.append("현재 배터리: ")
                    .append(chargeInfo.getCurrentSoc())
                    .append("%\n");

            promptBuilder.append("목표 배터리: ")
                    .append(chargeInfo.getTargetSoc())
                    .append("%\n");

            promptBuilder.append("필요 충전량: ")
                    .append(chargeInfo.getRequiredKwh())
                    .append("kWh\n");

            promptBuilder.append("충전소명: ")
                    .append(chargeInfo.getStationName())
                    .append("\n");

            promptBuilder.append("충전소 주소: ")
                    .append(chargeInfo.getStationAddress())
                    .append("\n");

            promptBuilder.append("거리: ")
                    .append(chargeInfo.getDistanceKm())
                    .append("km\n");

            promptBuilder.append("충전기명: ")
                    .append(chargeInfo.getChargerName())
                    .append("\n");

            promptBuilder.append("충전기 유형: ")
                    .append(chargeInfo.getChargerType())
                    .append("\n");

            promptBuilder.append("충전 속도: ")
                    .append(chargeInfo.getChargingSpeedKw())
                    .append("kW\n");

            promptBuilder.append("요금: ")
                    .append(chargeInfo.getPricePerKwh())
                    .append("원/kWh\n");

            promptBuilder.append("예상 충전 시간: ")
                    .append(chargeInfo.getEstimatedMinutes())
                    .append("분\n");

            promptBuilder.append("예상 충전 비용: ")
                    .append(chargeInfo.getEstimatedCost())
                    .append("원\n");

        } else if ("CHARGE_TIME".equals(intentDTO.getIntent())
                || "CHARGE_COST".equals(intentDTO.getIntent())) {

            // 충전 계산 정보 없음
            promptBuilder.append("""

                    ===== 충전 시간/비용 계산 결과 =====
                    충전 시간 또는 비용 계산에 필요한 정보가 없다.
                    대표 차량, 기본 위치, 사용 가능한 충전기 정보가 없을 수 있다.
                    사용자에게 대표 차량과 기본 위치 설정 여부를 확인하도록 안내해라.

                    """);
        }

        // 현재 질문 추가
        promptBuilder.append("""

                ===== 현재 질문 =====
                """);

        promptBuilder.append(message);

        // Gemini 답변 생성
        return callGeminiApi(
                promptBuilder.toString()
        );
    }
    
    
 // Gemini 공통 호출
    private String callGeminiApi(String prompt) {
    	
    	 log.info("@# callGeminiApi()");
    	 
        String url = apiUrl + "?key=" + apiKey;

        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> body = new HashMap<>();

        body.put(
                "contents",
                List.of(
                        Map.of(
                                "parts",
                                List.of(
                                        Map.of(
                                                "text",
                                                prompt
                                        )
                                )
                        )
                )
        );

        HttpEntity<Map<String, Object>> request =
                new HttpEntity<>(body, headers);

        Map<String, Object> response =
                restTemplate.postForObject(
                        url,
                        request,
                        Map.class
                );

        if (response == null) {
            return "";
        }

        List<Map<String, Object>> candidates =
                (List<Map<String, Object>>) response.get("candidates");

        if (candidates == null || candidates.isEmpty()) {
            return "";
        }

        Map<String, Object> candidate =
                candidates.get(0);

        Map<String, Object> content =
                (Map<String, Object>) candidate.get("content");

        List<Map<String, Object>> parts =
                (List<Map<String, Object>>) content.get("parts");

        if (parts == null || parts.isEmpty()) {
            return "";
        }

        return String.valueOf(parts.get(0).get("text"));
    }
    
 // AI 최근 대화 Redis Key 생성
    private String getChatCacheKey(Long roomId) {
        return "ai:chat:recent:" + roomId;
    }

 // AI 답변 생성 시 사용할 최근 대화 조회
 // Redis 캐시 우선 사용, 캐시가 없으면 DB 조회 후 Redis 재적재
    private List<EvAiChatMessageDTO> getRecentMessages(Long roomId) {

        String key = getChatCacheKey(roomId);

        try {
            List<String> cachedList =
                    stringRedisTemplate.opsForList().range(key, 0, -1);

            if (cachedList != null && !cachedList.isEmpty()) {
                log.info("@# ai chat cache hit => roomId: {}", roomId);

                List<EvAiChatMessageDTO> messageList = new ArrayList<>();

                for (String cached : cachedList) {
                    EvAiChatMessageDTO dto =
                            objectMapper.readValue(cached, EvAiChatMessageDTO.class);

                    messageList.add(dto);
                }

                return messageList;
            }

            log.info("@# ai chat cache miss => roomId: {}", roomId);

        } catch (Exception e) {
            log.warn("@# ai chat cache read fail => {}", e.getMessage());
        }

        List<EvAiChatMessageDTO> dbList =
                evAiChatDAO.findRecentMessagesByRoomId(roomId);

        saveRecentMessagesToCache(roomId, dbList);

        return dbList;
    }

    // DB 최근 대화 Redis 저장
    private void saveRecentMessagesToCache(
            Long roomId,
            List<EvAiChatMessageDTO> messageList) {

        if (messageList == null || messageList.isEmpty()) {
            return;
        }

        String key = getChatCacheKey(roomId);

        try {
            stringRedisTemplate.delete(key);

            for (EvAiChatMessageDTO dto : messageList) {
                String json = objectMapper.writeValueAsString(dto);
                stringRedisTemplate.opsForList().rightPush(key, json);
            }

            stringRedisTemplate.opsForList().trim(key, -CHAT_CACHE_LIMIT, -1);
            stringRedisTemplate.expire(key, CHAT_CACHE_TTL);

            log.info("@# ai chat cache save => roomId: {}, count: {}",
                    roomId, messageList.size());

        } catch (Exception e) {
            log.warn("@# ai chat cache save fail => {}", e.getMessage());
        }
    }

    // 새 메시지 Redis 최근 대화에 추가
    private void addMessageToCache(
            Long roomId,
            EvAiChatMessageDTO messageDTO) {

        String key = getChatCacheKey(roomId);

        try {
            String json = objectMapper.writeValueAsString(messageDTO);

            stringRedisTemplate.opsForList().rightPush(key, json);
            stringRedisTemplate.opsForList().trim(key, -CHAT_CACHE_LIMIT, -1);
            stringRedisTemplate.expire(key, CHAT_CACHE_TTL);

            log.info("@# ai chat cache append => roomId: {}", roomId);

        } catch (Exception e) {
            log.warn("@# ai chat cache append fail => {}", e.getMessage());
        }
    }
    
    @Override
    public void clearChatCache(Long memberId) {

        log.info("@# clearChatCache()");
        log.info("@# memberId => {}", memberId);

        EvAiChatRoomDTO room =
                evAiChatDAO.findRoomByMemberId(memberId);

        if (room == null) {
            return;
        }

        String cacheKey =
                "ai:chat:room:" + room.getRoomId();

        stringRedisTemplate.delete(cacheKey);

        log.info("@# AI Redis Cache Deleted => {}", cacheKey);
    }
}