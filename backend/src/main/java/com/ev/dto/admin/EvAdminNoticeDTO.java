package com.ev.dto.admin;

import java.time.LocalDateTime;
import java.time.Duration;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class EvAdminNoticeDTO {

    private String message;
    private LocalDateTime createdAt;

    public String getTimeText() {
        if (createdAt == null) {
            return "-";
        }

        Duration duration = Duration.between(createdAt, LocalDateTime.now());

        long minutes = duration.toMinutes();
        long hours = duration.toHours();
        long days = duration.toDays();

        if (minutes < 1) {
            return "방금 전";
        }

        if (minutes < 60) {
            return minutes + "분 전";
        }

        if (hours < 24) {
            return hours + "시간 전";
        }

        return days + "일 전";
    }
}