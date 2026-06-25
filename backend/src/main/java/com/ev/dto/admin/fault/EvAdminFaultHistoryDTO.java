package com.ev.dto.admin.fault;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import lombok.Data;

/*
 * 장애 처리 이력 DTO
 */
@Data
public class EvAdminFaultHistoryDTO {

    private Long historyId;
    private Long faultId;
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
