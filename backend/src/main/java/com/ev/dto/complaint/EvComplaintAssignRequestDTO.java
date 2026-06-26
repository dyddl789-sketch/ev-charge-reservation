package com.ev.dto.complaint;

import lombok.Data;

/*
 * 관리자 민원 담당자 배정 요청 DTO
 */
@Data
public class EvComplaintAssignRequestDTO {

    private Long assignedDepartmentId;
    private Long assignedEmployeeId;
    private String memo;
}
