package com.ev.dto.complaint;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import lombok.Data;

/*
 * 민원 목록/상세 응답 DTO
 */
@Data
public class EvComplaintDTO {

    private Long complaintId;
    private Long memberId;

    private String memberName;
    private String userId;
    private String email;
    private String phone;

    private String title;
    private String content;
    private String complaintType;
    private String priority;
    private String status;

    private Long stationId;
    private Long chargerId;
    private String stationName;
    private String chargerName;

    private Boolean notifyEmail;
    private Boolean notifySms;
    private Boolean notifySite;

    private Long assignedDepartmentId;
    private String assignedDepartmentName;
    private Long assignedEmployeeId;
    private String assignedEmployeeName;

    private String aiSummary;
    private String adminMemo;

    // 사용자에게 표시할 처리 답변은 현재 DB의 admin_memo를 사용한다.
    private String answerContent;

    // 민원에서 장애 접수 시 연결되는 장애 번호/상태
    private Long linkedFaultId;
    private String linkedFaultStatus;

    // AI 분류 라벨 표시용 필드
    private String aiCategory;
    private String aiLabel;
    private Double aiConfidence;
    private String aiReason;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime closedAt;

    private List<EvComplaintHistoryDTO> historyList;

    public String getCreatedAtText() {
        return formatDateTime(createdAt);
    }

    public String getUpdatedAtText() {
        return formatDateTime(updatedAt);
    }

    public String getClosedAtText() {
        return formatDateTime(closedAt);
    }

    private String formatDateTime(LocalDateTime value) {
        if (value == null) {
            return "-";
        }

        return value.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
    }
}
