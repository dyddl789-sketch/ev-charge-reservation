package com.ev.dto.admin;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import lombok.Data;

/*
 * 관리자 회원 목록 행 DTO
 */
@Data
public class EvAdminMemberRowDTO {

    private Long memberId;

    private String userId;
    private String memberName;
    private String email;
    private String phone;

    private String userType;
    private String loginType;
    private String status;

    private LocalDateTime createdAt;

    public String getCreatedAtText() {
        if (createdAt == null) {
            return "-";
        }

        return createdAt.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
    }

    public String getUserTypeText() {
        if ("ADMIN".equals(userType)) {
            return "관리자";
        }

        return "회원";
    }

    public String getUserTypeClass() {
        if ("ADMIN".equals(userType)) {
            return "admin";
        }

        return "user";
    }

    public String getStatusText() {
        if ("ACTIVE".equals(status)) {
            return "활성";
        }

        if ("INACTIVE".equals(status)) {
            return "탈퇴";
        }

        if ("BLOCKED".equals(status)) {
            return "정지";
        }

        return status;
    }

    public String getStatusClass() {
        if ("ACTIVE".equals(status)) {
            return "active";
        }

        if ("INACTIVE".equals(status)) {
            return "withdraw";
        }

        if ("BLOCKED".equals(status)) {
            return "blocked";
        }

        return "withdraw";
    }
}