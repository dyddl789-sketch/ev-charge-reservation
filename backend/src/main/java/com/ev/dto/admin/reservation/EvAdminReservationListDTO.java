package com.ev.dto.admin.reservation;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import lombok.Data;

/*
 * 관리자 예약 목록 행 DTO
 */
@Data
public class EvAdminReservationListDTO {

    private Long reservationId;
    private String reservationNo;

    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String memberName;
    private String userId;
    private String vehicleName;
    private String stationName;
    private String stationAddress;
    private String chargerName;

    private Integer currentSoc;
    private Integer targetSoc;
    private Integer estimatedMinutes;
    private Long estimatedCost;

    private String authCode;
    private String status;

    public String getStartTimeText() {
        if (startTime == null) {
            return "-";
        }
        return startTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
    }

    public String getSocText() {
        return currentSoc + "% → " + targetSoc + "%";
    }

    public String getStatusClass() {
        if ("예약완료".equals(status)) return "reserved";
        if ("인증완료".equals(status)) return "verified";
        if ("충전중".equals(status)) return "charging";
        if ("완료".equals(status)) return "completed";
        if ("취소".equals(status)) return "canceled";
        if ("노쇼".equals(status)) return "noshow";
        return "";
    }
}