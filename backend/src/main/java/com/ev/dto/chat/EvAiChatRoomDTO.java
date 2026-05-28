package com.ev.dto.chat;

import java.time.LocalDateTime;

import lombok.Data;

// AI 채팅방 DTO
@Data
public class EvAiChatRoomDTO {

    // 채팅방 번호
    private Long roomId;

    // 회원 번호
    private Long memberId;

    // 채팅방 제목
    private String title;

    // 생성일
    private LocalDateTime createdAt;

    // 수정일
    private LocalDateTime updatedAt;
}