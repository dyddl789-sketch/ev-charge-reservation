package com.ev.dto.admin.employee;

import java.time.LocalDate;
import java.time.LocalDateTime;

import lombok.Data;

/*
 * 관리자 인사관리 직원 DTO
 * app_member + employee 조인 결과와 등록/수정 요청을 함께 처리한다.
 */
@Data
public class EvAdminEmployeeDTO {

    private Long employeeId;
    private Long memberId;
    private Long departmentId;

    private String userId;
    private String password;
    private String memberName;
    private String nickname;
    private String phone;
    private String email;
    private String userType;
    private String loginType;

    private String employeeNo;
    private String departmentName;
    private String departmentCode;
    private String positionName;
    private String dutyName;
    private String status;

    private LocalDate hiredAt;
    private LocalDate retiredAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
