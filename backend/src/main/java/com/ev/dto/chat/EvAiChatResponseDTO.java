package com.ev.dto.chat;

import lombok.AllArgsConstructor;
import lombok.Data;

// AI 응답 DTO
@Data
@AllArgsConstructor
public class EvAiChatResponseDTO {

    // AI 답변
    private String answer;

}