package com.ev.dto.complaint;

import lombok.Data;

/*
 * 사용자 민원 등록 요청 DTO
 */
@Data
public class EvComplaintRequestDTO {

    private Long complaintId;
    private Long memberId;

    private String title;
    private String content;
    private String complaintType;
    private String priority;

    private Long stationId;
    private Long chargerId;
    private String stationName;
    private String chargerName;

    private Boolean notifyEmail;
    private Boolean notifySms;
    private Boolean notifySite;
}
