package com.ev.dto.admin;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import lombok.Data;

@Data
public class EvAdminMemberChargingDTO {

    private Long sessionId;
    private Long reservationId;
    private String stationName;
    private String chargerName;
    private String vehicleNickname;
    private String modelName;
    private LocalDateTime actualStartTime;
    private LocalDateTime actualEndTime;
    private Double actualKwh;
    private Long actualCost;
    private String status;

    public String getActualStartTimeText() {
        if (actualStartTime == null) return "-";
        return actualStartTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
    }

    public String getActualEndTimeText() {
        if (actualEndTime == null) return "-";
        return actualEndTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
    }
}