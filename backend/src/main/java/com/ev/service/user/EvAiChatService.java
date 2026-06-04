package com.ev.service.user;

import java.util.List;

import com.ev.dto.chat.EvAiChatMessageDTO;
import com.ev.dto.chat.EvAiChatResponseDTO;

public interface EvAiChatService {

    // AI 메시지 전송 및 DB 저장
    EvAiChatResponseDTO sendMessage(Long memberId, String message);
    
    // 이전 채팅 메시지 조회
    List<EvAiChatMessageDTO> getChatHistory(Long memberId);
}