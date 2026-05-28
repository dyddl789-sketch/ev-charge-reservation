package com.ev.dto.chat;

import lombok.Data;

// 사용자 채팅 요청 DTO
@Data
public class EvAiChatRequestDTO {

    // 사용자가 입력한 메시지
    private String message;

}