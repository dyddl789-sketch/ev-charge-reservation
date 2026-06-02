package com.ev.dto.admin;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import lombok.Data;

@Data
public class EvAdminMemberReservationDTO {

    private Long reservationId;
    private String stationName;
    private String chargerName;
    private String vehicleNickname;
    private String modelName;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String status;
    private Long estimatedCost;

    public String getStartTimeText() {
        if (startTime == null) return "-";
        return startTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
    }

    public String getEndTimeText() {
        if (endTime == null) return "-";
        return endTime.format(DateTimeFormatter.ofPattern("HH:mm"));
    }
}