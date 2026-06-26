package com.ev.dto.complaint;

import lombok.Data;

/*
 * 관리자 민원 답변 완료 요청 DTO
 */
@Data
public class EvComplaintAnswerRequestDTO {

    private String answerContent;
    private String memo;
}
