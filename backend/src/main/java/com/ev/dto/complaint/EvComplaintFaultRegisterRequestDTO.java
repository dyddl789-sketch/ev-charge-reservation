package com.ev.dto.complaint;

import lombok.Data;

/*
 * 민원 기반 장애 접수 요청 DTO
 */
@Data
public class EvComplaintFaultRegisterRequestDTO {

    private String answerContent;
    private String memo;
    private String faultType;
    private String title;
    private String description;
}
