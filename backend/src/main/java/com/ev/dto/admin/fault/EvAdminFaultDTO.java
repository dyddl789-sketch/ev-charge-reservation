package com.ev.dto.admin.fault;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import lombok.Data;

/*
 * 관리자 장애·점검 목록/상세 DTO
 */
@Data
public class EvAdminFaultDTO {

    private Long faultId;
    private Long complaintId;
    private Long stationId;
    private Long chargerId;
    private Long assignedEmployeeId;
    private Long latestInspectionId;
    private Long latestActionId;
    private Long approvalDocumentId;


    private String faultType;
    private String title;
    private String description;
    private String severity;
    private String status;
    private String sourceType;

    private String stationName;
    private String chargerName;
    private String chargerType;
    private String connectorType;
    private String chargerStatus;
    private String address;

    private String assignedEmployeeName;
    private String assignedEmployeeNo;
    private String assignedDepartmentName;

    private String latestInspectionResult;
    private String latestInspectionDescription;
    private String latestActionStatus;
    private String latestActionType;
    private String latestActionResult;
    private String approvalStatus;
    private String approvalTitle;


    private LocalDateTime reportedAt;
    private LocalDateTime resolvedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime latestInspectionCompletedAt;
    private LocalDateTime latestActionCompletedAt;

    private List<EvAdminFaultHistoryDTO> historyList;

    public String getReportedAtText() {
        return formatDateTime(reportedAt);
    }

    public String getResolvedAtText() {
        return formatDateTime(resolvedAt);
    }

    public String getUpdatedAtText() {
        return formatDateTime(updatedAt);
    }

    public String getLatestInspectionCompletedAtText() {
        return formatDateTime(latestInspectionCompletedAt);
    }

    public String getLatestActionCompletedAtText() {
        return formatDateTime(latestActionCompletedAt);
    }

    private String formatDateTime(LocalDateTime value) {
        if (value == null) {
            return "-";
        }

        return value.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
    }
}
