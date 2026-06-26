package com.ev.dto.admin.fault;

import lombok.Data;

/*
 * 장애 담당자 배정 요청 DTO
 */
@Data
public class EvAdminFaultAssignRequestDTO {

    private Long employeeId;
    private String memo;
}
