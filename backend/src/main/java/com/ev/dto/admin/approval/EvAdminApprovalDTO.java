package com.ev.dto.admin.approval;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import lombok.Data;

/*
 * 전자결재 문서 목록/상세 DTO
 */
@Data
public class EvAdminApprovalDTO {

    private Long documentId;
    private Long writerId;
    private Long faultId;
    private Long inspectionId;

    private String documentType;
    private String title;
    private String content;
    private BigDecimal estimatedCost;
    private String status;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime completedAt;

    private String writerName;
    private String writerEmployeeNo;
    private String writerUserType;
    private String writerDepartmentName;
    private String writerPositionName;

    private String faultTitle;
    private String faultStatus;
    private String faultSeverity;
    private String stationName;
    private String chargerName;
    private String chargerType;
    private String connectorType;
    private String chargerStatus;
    private String inspectionResult;
    private String inspectionDescription;

    private Long currentApproverId;
    private String currentApproverName;
    private String currentApproverUserType;

    private List<EvAdminApprovalLineDTO> lineList;
    private List<EvAdminApprovalHistoryDTO> historyList;

    public String getDocumentNo() {
        if (documentId == null) {
            return "-";
        }

        return String.format("APR-%06d", documentId);
    }

    public String getCreatedAtText() {
        return formatDateTime(createdAt);
    }

    public String getUpdatedAtText() {
        return formatDateTime(updatedAt);
    }

    public String getCompletedAtText() {
        return formatDateTime(completedAt);
    }

    private String formatDateTime(LocalDateTime value) {
        if (value == null) {
            return "-";
        }

        return value.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
    }
}
