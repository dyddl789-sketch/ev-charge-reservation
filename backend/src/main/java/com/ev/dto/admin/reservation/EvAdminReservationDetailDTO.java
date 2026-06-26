package com.ev.dto.admin.reservation;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import lombok.Data;

/*
 * 관리자 예약 상세 DTO
 */
@Data
public class EvAdminReservationDetailDTO {

    private Long reservationId;
    private String reservationNo;

    private Long memberId;
    private String memberName;
    private String phone;
    private String email;

    private String vehicleName;
    private String plateNumber;

    private String stationName;
    private String stationAddress;
    private String chargerName;
    private String chargerType;
    private String connectorType;

    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Integer currentSoc;
    private Integer targetSoc;
    private Integer estimatedMinutes;
    private Long estimatedCost;
    private String authCode;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public String getStartTimeText() {
        return formatDateTime(startTime);
    }

    public String getEndTimeText() {
        return formatDateTime(endTime);
    }

    public String getCreatedAtText() {
        return formatDateTime(createdAt);
    }

    public String getUpdatedAtText() {
        return formatDateTime(updatedAt);
    }

    private String formatDateTime(LocalDateTime value) {
        if (value == null) return "-";
        return value.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
    }
}
