package com.ev.dto.complaint;

import lombok.Data;

/*
 * 관리자 민원 검색 조건 DTO
 */
@Data
public class EvComplaintSearchDTO {

    private String status;
    private String complaintType;
    private String priority;
    private String keyword;
    private Long stationId;
    private Long chargerId;
}
