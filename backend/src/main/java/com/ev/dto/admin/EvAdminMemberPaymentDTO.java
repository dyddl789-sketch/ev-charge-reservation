package com.ev.dto.admin;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import lombok.Data;

@Data
public class EvAdminMemberPaymentDTO {

    private Long sessionId;
    private Long reservationId;
    private String stationName;
    private Double actualKwh;
    private Long actualCost;
    private LocalDateTime paidAt;
    private String status;

    public String getPaidAtText() {
        if (paidAt == null) return "-";
        return paidAt.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
    }
}