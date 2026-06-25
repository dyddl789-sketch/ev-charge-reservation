package com.ev.dto.admin.approval;

import java.math.BigDecimal;

import lombok.Data;

/*
 * 장애 점검 결과 교체필요 시 전자결재 상신 요청 DTO
 */
@Data
public class EvAdminApprovalSubmitRequestDTO {

    private String documentType;
    private String title;
    private String content;
    private BigDecimal estimatedCost;
}
