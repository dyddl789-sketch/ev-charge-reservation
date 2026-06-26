package com.ev.dto.admin.approval;

import lombok.Data;

/*
 * 전자결재 승인/반려 요청 DTO
 */
@Data
public class EvAdminApprovalDecisionRequestDTO {

    private String comment;
    private String signatureData;
}
