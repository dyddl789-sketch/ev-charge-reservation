package com.ev.dto.admin.approval;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import lombok.Data;

/*
 * 전자결재 처리 이력 DTO
 */
@Data
public class EvAdminApprovalHistoryDTO {

    private Long historyId;
    private Long documentId;
    private Long employeeId;

    private String employeeName;
    private String actionType;
    private String beforeStatus;
    private String afterStatus;
    private String comment;

    private LocalDateTime createdAt;

    public String getCreatedAtText() {
        if (createdAt == null) {
            return "-";
        }

        return createdAt.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
    }
}
