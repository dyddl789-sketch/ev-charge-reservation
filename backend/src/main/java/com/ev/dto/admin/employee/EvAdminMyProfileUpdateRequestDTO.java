package com.ev.dto.admin.employee;

import lombok.Data;

/*
 * MIS 내 정보 수정 요청 DTO
 */
@Data
public class EvAdminMyProfileUpdateRequestDTO {

    private String email;
    private String phone;

    private String currentPassword;
    private String newPassword;
    private String newPasswordConfirm;
}
