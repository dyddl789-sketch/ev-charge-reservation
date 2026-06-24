package com.ev.dto.complaint;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import lombok.Data;

/*
 * 민원 목록/상세 응답 DTO
 */
@Data
public class EvComplaintDTO {

    private Long complaintId;
    private Long memberId;

    private String memberName;
    private String userId;
    private String email;
    private String phone;

    private String title;
    private String content;
    private String complaintType;
    private String priority;
    private String status;

    private Long stationId;
    private Long chargerId;
    private String stationName;
    private String chargerName;

    private Boolean notifyEmail;
    private Boolean notifySms;
    private Boolean notifySite;

    private Long assignedDepartmentId;
    private String assignedDepartmentName;
    private Long assignedEmployeeId;
    private String assignedEmployeeName;

    private String aiSummary;
    private String adminMemo;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime closedAt;

    private List<EvComplaintHistoryDTO> historyList;

    public String getCreatedAtText() {
        return formatDateTime(createdAt);
    }

    public String getUpdatedAtText() {
        return formatDateTime(updatedAt);
    }

    public String getClosedAtText() {
        return formatDateTime(closedAt);
    }

    private String formatDateTime(LocalDateTime value) {
        if (value == null) {
            return "-";
        }

        return value.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
    }
}
