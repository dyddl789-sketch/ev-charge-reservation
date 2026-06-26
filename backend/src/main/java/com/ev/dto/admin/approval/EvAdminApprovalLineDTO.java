package com.ev.dto.admin.approval;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import lombok.Data;

/*
 * 전자결재 결재선 DTO
 */
@Data
public class EvAdminApprovalLineDTO {

    private Long lineId;
    private Long documentId;
    private Long approverId;
    private Integer approvalOrder;

    private String status;
    private String comment;
    private String signatureData;

    private LocalDateTime approvedAt;

    private String approverName;
    private String approverUserType;
    private String employeeNo;
    private String departmentName;
    private String positionName;

    public String getApprovedAtText() {
        if (approvedAt == null) {
            return "-";
        }

        return approvedAt.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
    }
}
