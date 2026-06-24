package com.ev.dto.complaint;

import lombok.Data;

/*
 * 관리자 민원 상태 변경 요청 DTO
 */
@Data
public class EvComplaintStatusRequestDTO {

    private String status;
    private String memo;
    private String adminMemo;
}
