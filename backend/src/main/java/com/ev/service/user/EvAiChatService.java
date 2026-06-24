package com.ev.service.user;

import java.util.List;

import com.ev.dto.chat.EvAiChatMessageDTO;
import com.ev.dto.chat.EvAiChatResponseDTO;

public interface EvAiChatService {

    // AI 메시지 전송 및 DB 저장
    EvAiChatResponseDTO sendMessage(Long memberId, String message);
    
    // 이전 채팅 메시지 조회
    List<EvAiChatMessageDTO> getChatHistory(Long memberId);
    
    // Redis 대화 캐시와 AI 예약 후보만 초기화
    void clearChatCache(Long memberId);

    // 사용자의 AI 대화 메시지 DB 이력과 Redis 캐시를 함께 초기화
    void clearChatMessages(Long memberId);
}
