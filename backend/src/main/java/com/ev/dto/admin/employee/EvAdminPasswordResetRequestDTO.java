package com.ev.dto.admin.employee;

import lombok.Data;

/*
 * 관리자 직원 비밀번호 초기화 요청 DTO
 */
@Data
public class EvAdminPasswordResetRequestDTO {

    private String newPassword;
    private String newPasswordConfirm;
}
