package com.ev.dto.admin;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import lombok.Data;

/*
 * 관리자 회원 상세 정보 DTO
 */
@Data
public class EvAdminMemberDetailDTO {

    private Long memberId;
    private String userId;
    private String memberName;
    private String nickname;
    private String phone;
    private String email;
    private String userType;
    private String loginType;
    private String status;
    private LocalDateTime lastLoginAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private Long vehicleCount;
    private Long reservationCount;
    private Long completedSessionCount;
    private Long totalPaymentAmount;

    public String getCreatedAtText() {
        return formatDate(createdAt);
    }

    public String getLastLoginAtText() {
        return formatDateTime(lastLoginAt);
    }

    public String getUserTypeText() {
        return "ADMIN".equals(userType) ? "관리자" : "회원";
    }

    public String getLoginTypeText() {
        return loginType == null ? "-" : loginType;
    }

    public String getStatusText() {
        if ("ACTIVE".equals(status)) return "활성";
        if ("INACTIVE".equals(status)) return "탈퇴";
        if ("BLOCKED".equals(status)) return "정지";
        return status;
    }

    public String getStatusClass() {
        if ("ACTIVE".equals(status)) return "active";
        if ("BLOCKED".equals(status)) return "blocked";
        return "withdraw";
    }

    private String formatDate(LocalDateTime dateTime) {
        if (dateTime == null) return "-";
        return dateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
    }

    private String formatDateTime(LocalDateTime dateTime) {
        if (dateTime == null) return "-";
        return dateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
    }
    
    public String getInitial() {
        if (memberName == null || memberName.isBlank()) {
            return "?";
        }

        return memberName.substring(0, 1);
    }
}