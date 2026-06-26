package com.ev.dto.admin.fault;

import lombok.Data;

/*
 * 점검 결과 저장 요청 DTO
 */
@Data
public class EvAdminInspectionResultRequestDTO {

    private String inspectionResult;
    private String description;
}
