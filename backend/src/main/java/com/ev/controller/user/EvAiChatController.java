package com.ev.controller.user;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ev.dto.chat.EvAiChatMessageDTO;
import com.ev.dto.chat.EvAiChatRequestDTO;
import com.ev.dto.chat.EvAiChatResponseDTO;
import com.ev.security.EvUserDetails;
import com.ev.service.user.EvAiChatService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/ai-chat")
@RequiredArgsConstructor
public class EvAiChatController {

    private final EvAiChatService evAiChatService;

    // 기존 백엔드 API: AI 채팅 메시지 전송
    @PostMapping("/send")
    public ResponseEntity<?> sendMessage(
            @AuthenticationPrincipal EvUserDetails userDetails,
            @RequestBody EvAiChatRequestDTO requestDTO) {
        return sendMessageInternal(userDetails, requestDTO);
    }

    // React API 별칭: frontend/src/apis/aiApi.js에서 사용
    @PostMapping("/message")
    public ResponseEntity<?> sendMessageAlias(
            @AuthenticationPrincipal EvUserDetails userDetails,
            @RequestBody EvAiChatRequestDTO requestDTO) {
        return sendMessageInternal(userDetails, requestDTO);
    }

    // 기존 백엔드 API: 이전 채팅 메시지 조회
    @GetMapping("/history")
    public ResponseEntity<?> getChatHistory(
            @AuthenticationPrincipal EvUserDetails userDetails) {
        return getHistoryInternal(userDetails);
    }

    // React API 별칭
    @GetMapping("/messages")
    public ResponseEntity<?> getMessages(
            @AuthenticationPrincipal EvUserDetails userDetails) {
        return getHistoryInternal(userDetails);
    }

    // React 화면에서 채팅방 존재 여부 확인용 단순 API
    @GetMapping("/room")
    public ResponseEntity<?> getRoom(
            @AuthenticationPrincipal EvUserDetails userDetails) {

        if (userDetails == null) {
            return ResponseEntity.status(401).body(Map.of("message", "로그인이 필요합니다."));
        }

        return ResponseEntity.ok(Map.of(
                "success", true,
                "memberId", userDetails.getMemberId(),
                "title", "AI 채팅"
        ));
    }

    // React API: AI 대화 이력과 Redis 캐시 전체 초기화
    @DeleteMapping("/messages")
    public ResponseEntity<?> clearMessages(
            @AuthenticationPrincipal EvUserDetails userDetails) {

        if (userDetails == null) {
            return ResponseEntity.status(401).body(Map.of("message", "로그인이 필요합니다."));
        }

        evAiChatService.clearChatMessages(userDetails.getMemberId());

        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "AI 대화 이력과 Redis 캐시가 초기화되었습니다."
        ));
    }

    private ResponseEntity<?> sendMessageInternal(EvUserDetails userDetails, EvAiChatRequestDTO requestDTO) {
        log.info("@# EvAiChatController.sendMessageInternal()");

        if (userDetails == null) {
            return ResponseEntity.status(401).body(Map.of("message", "로그인이 필요합니다."));
        }

        if (requestDTO == null || requestDTO.getMessage() == null || requestDTO.getMessage().trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("message", "메시지를 입력해주세요."));
        }

        Long memberId = userDetails.getMemberId();
        EvAiChatResponseDTO responseDTO = evAiChatService.sendMessage(memberId, requestDTO.getMessage());

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("answer", responseDTO.getAnswer());
        response.put("message", responseDTO.getAnswer());
        response.put("senderType", "AI");
        response.put("intent", responseDTO.getIntent());

        if (responseDTO.getLocation() != null) {
            response.put("location", responseDTO.getLocation());
        }

        if (responseDTO.getReservations() != null) {
            response.put("reservations", responseDTO.getReservations());
        }

        return ResponseEntity.ok(response);
    }

    private ResponseEntity<?> getHistoryInternal(EvUserDetails userDetails) {
        log.info("@# EvAiChatController.getHistoryInternal()");

        if (userDetails == null) {
            return ResponseEntity.status(401).body(Map.of("message", "로그인이 필요합니다."));
        }

        Long memberId = userDetails.getMemberId();
        List<EvAiChatMessageDTO> messageList = evAiChatService.getChatHistory(memberId);

        return ResponseEntity.ok(messageList);
    }
}
