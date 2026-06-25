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

    // 프론트에서 카드 UI를 구분하기 위한 의도값
    private String intent;

    // 프론트 액션 버튼 타입: VEHICLE_REGISTER, MY_RESERVATION_HISTORY 등
    private String actionType;

    // 프론트 액션 버튼 문구
    private String buttonText;

    // 프론트 이동 URL
    private String actionUrl;

    // 지도 이동용 위치 JSON
    private String locationJson;

    // AI 예약 후보 카드 JSON
    private String candidatesJson;

    // 내 예약 카드 JSON
    private String reservationsJson;

    // 생성일
    private LocalDateTime createdAt;
}
