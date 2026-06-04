package com.ev.dto.chat;

import java.time.LocalDateTime;

import lombok.Data;

// AI 채팅 메시지 DTO
@Data
public class EvAiChatMessageDTO {

    // 메시지 번호
    private Long messageId;

    // 채팅방 번호
    private Long roomId;

    // 발신자 타입(USER / AI)
    private String senderType;

    // 메시지 내용
    private String message;

    // 생성일
    private LocalDateTime createdAt;
}