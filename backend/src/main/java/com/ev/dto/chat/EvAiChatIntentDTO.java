package com.ev.dto.chat;

import lombok.Data;



@Data
public class EvAiChatIntentDTO {

    // 분석된 사용자 의도
    private String intent;

    // 현재 배터리 잔량
    private Integer currentSoc;

    // 목표 배터리 잔량
    private Integer targetSoc;

    // 추천 우선순위: DISTANCE, SPEED, COST
    private String priority;
}