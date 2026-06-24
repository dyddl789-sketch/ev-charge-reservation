package com.ev.dto.admin.employee;

import lombok.Data;

/*
 * 관리자 인사관리 부서 DTO
 */
@Data
public class EvAdminDepartmentDTO {

    private Long departmentId;
    private String departmentName;
    private String departmentCode;
    private String description;
    private Boolean isActive;
}
