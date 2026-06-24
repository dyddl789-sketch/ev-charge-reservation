package com.ev.dto.chat;

import java.util.List;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// AI 응답 DTO
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EvAiChatResponseDTO {

    // AI 답변
    private String answer;

    // 프론트에서 카드 UI를 구분하기 위한 의도값
    private String intent;

    // 기본 출발지 지도 이동용 데이터
    private Map<String, Object> location;

    // 내 예약 조회 카드 데이터
    private List<Map<String, Object>> reservations;

    public EvAiChatResponseDTO(String answer) {
        this.answer = answer;
    }

    public EvAiChatResponseDTO(String answer, String intent) {
        this.answer = answer;
        this.intent = intent;
    }
}
