package com.ev.dto.complaint;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import lombok.Data;

/*
 * 민원 처리 이력 DTO
 */
@Data
public class EvComplaintHistoryDTO {

    private Long historyId;
    private Long complaintId;
    private Long employeeId;

    private String employeeName;
    private String beforeStatus;
    private String afterStatus;
    private String actionType;
    private String memo;

    private LocalDateTime createdAt;

    public String getCreatedAtText() {
        if (createdAt == null) {
            return "-";
        }

        return createdAt.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
    }
}
