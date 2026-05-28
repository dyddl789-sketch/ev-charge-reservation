package com.ev.controller.user;

import java.util.List;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
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

    // AI 채팅 메시지 전송
    @PostMapping("/send")
    public EvAiChatResponseDTO sendMessage(
            @AuthenticationPrincipal EvUserDetails userDetails,
            @RequestBody EvAiChatRequestDTO requestDTO) {

        log.info("@# EvAiChatController.sendMessage()");

        Long memberId = userDetails.getMemberId();

        log.info("@# memberId => {}", memberId);
        log.info("@# message => {}", requestDTO.getMessage());

        return evAiChatService.sendMessage(
                memberId,
                requestDTO.getMessage()
        );
    }

    // 이전 채팅 메시지 조회
    @GetMapping("/history")
    public List<EvAiChatMessageDTO> getChatHistory(
            @AuthenticationPrincipal EvUserDetails userDetails) {

        log.info("@# EvAiChatController.getChatHistory()");

        Long memberId = userDetails.getMemberId();

        log.info("@# memberId => {}", memberId);

        return evAiChatService.getChatHistory(memberId);
    }
}