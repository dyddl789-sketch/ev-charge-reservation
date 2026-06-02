package com.ev.dto.admin;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import lombok.Data;

/*
 * 관리자 대시보드 오늘 예약 목록 DTO
 */
@Data
public class EvAdminReservationRowDTO {

    private Long reservationId;

    private String memberName;
    private String modelName;
    private String stationName;
    private String chargerName;

    private LocalDateTime startTime;
    private LocalDateTime endTime;

    private String status;

    public String getReservationTimeText() {
        if (startTime == null || endTime == null) {
            return "-";
        }

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");

        return startTime.format(formatter) + " ~ " + endTime.format(formatter);
    }

    public String getStatusText() {
        if ("예약완료".equals(status)) {
            return "예약 확정";
        }

        return status;
    }

    public String getStatusClass() {
        if ("충전중".equals(status)) {
            return "using";
        }

        if ("완료".equals(status)) {
            return "done";
        }

        if ("취소".equals(status) || "노쇼".equals(status)) {
            return "cancel";
        }

        return "reserved";
    }
}