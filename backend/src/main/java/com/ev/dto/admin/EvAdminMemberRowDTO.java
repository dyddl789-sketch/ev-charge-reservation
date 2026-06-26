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
    private String nickname;
    private String email;
    private String phone;
    private String profileImageUrl;

    private String userType;
    private String loginType;
    private String status;

    private Long vehicleCount;
    private Long reservationCount;
    private Long completedSessionCount;
    private Long totalPaymentAmount;

    private LocalDateTime createdAt;

    public String getCreatedAtText() {
        if (createdAt == null) {
            return "-";
        }

        return createdAt.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
    }

    public String getUserTypeText() {
        if ("ADMIN".equals(userType)) return "최고관리자";
        if ("MANAGER".equals(userType)) return "운영관리자";
        if ("OPERATOR".equals(userType)) return "운영담당자";
        if ("ENGINEER".equals(userType)) return "시설관리담당자";
        return "회원";
    }

    public String getStatusText() {
        if ("ACTIVE".equals(status)) return "활성";
        if ("INACTIVE".equals(status)) return "탈퇴";
        if ("BLOCKED".equals(status)) return "정지";
        return status;
    }
}
