package com.ev.service.user;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import com.ev.dao.user.EvAiChatDAO;
import com.ev.dto.chat.EvAiChatMessageDTO;
import com.ev.dto.chat.EvAiChatResponseDTO;
import com.ev.dto.chat.EvAiChatRoomDTO;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@SuppressWarnings("unchecked")
public class EvAiChatServiceImpl implements EvAiChatService {

    private final EvAiChatDAO evAiChatDAO;

    // Gemini API Key
    @Value("${gemini.api.key}")
    private String apiKey;

    // Gemini API URL
    @Value("${gemini.api.url}")
    private String apiUrl;

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
                evAiChatDAO.findRecentMessagesByRoomId(roomId);

        // Gemini 호출
        String answer =
                callGemini(recentMessages, message);

        // AI 답변 저장
        saveMessage(roomId, "AI", answer);

        // AI 답변 반환
        return new EvAiChatResponseDTO(answer);
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
                evAiChatDAO.findMessagesByRoomId(roomId);

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

        evAiChatDAO.insertMessage(messageDTO);
    }

    // Gemini API 호출
    private String callGemini(
            List<EvAiChatMessageDTO> recentMessages,
            String message) {

        log.info("@# EvAiChatServiceImpl.callGemini()");

        // Gemini 요청 URL
        String url = apiUrl + "?key=" + apiKey;

        // RestTemplate 생성
        RestTemplate restTemplate = new RestTemplate();

        // HTTP Header 설정
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

     // 이전 대화 프롬프트 생성
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

        // 현재 질문 추가
        promptBuilder.append("""

                ===== 현재 질문 =====
                """);

        promptBuilder.append(message);

        // Gemini 요청 Body 생성
        Map<String, Object> body = new HashMap<>();

        body.put(
                "contents",
                List.of(
                        Map.of(
                                "parts",
                                List.of(
                                        Map.of(
                                                "text",
                                                promptBuilder.toString()
                                        )
                                )
                        )
                )
        );

        // HTTP 요청 객체 생성
        HttpEntity<Map<String, Object>> request =
                new HttpEntity<>(body, headers);

        // Gemini API 호출
        Map<String, Object> response =
                restTemplate.postForObject(
                        url,
                        request,
                        Map.class
                );

        log.info("@# Gemini response => {}", response);

        // 응답 없으면 기본 메시지 반환
        if (response == null) {
            return "AI 응답을 가져오지 못했습니다.";
        }

        // candidates 추출
        List<Map<String, Object>> candidates =
                (List<Map<String, Object>>) response.get("candidates");

        // candidates 비어있으면 기본 메시지 반환
        if (candidates == null || candidates.isEmpty()) {
            return "AI 응답을 가져오지 못했습니다.";
        }

        // 첫 번째 응답 선택
        Map<String, Object> candidate =
                candidates.get(0);

        // content 추출
        Map<String, Object> content =
                (Map<String, Object>) candidate.get("content");

        // parts 추출
        List<Map<String, Object>> parts =
                (List<Map<String, Object>>) content.get("parts");

        // parts 비어있으면 기본 메시지 반환
        if (parts == null || parts.isEmpty()) {
            return "AI 응답을 가져오지 못했습니다.";
        }

        // text 추출
        String answer =
                String.valueOf(parts.get(0).get("text"));

        log.info("@# answer => {}", answer);

        return answer;
    }
}