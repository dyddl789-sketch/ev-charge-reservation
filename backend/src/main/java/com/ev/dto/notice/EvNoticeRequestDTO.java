package com.ev.dto.notice;

import lombok.Data;

/*
 * 관리자 공지사항 등록/수정 요청 DTO
 */
@Data
public class EvNoticeRequestDTO {

    // 공지사항 고유 번호
    private Long noticeId;

    // 공지 제목
    private String title;

    // 공지 내용
    private String content;

    // 카테고리: 공지, 점검, 안내 등
    private String category;

    // 게시 상태: 임시저장, 게시, 숨김, 삭제
    private String status;

    // 상단 고정 여부
    private Boolean isPinned;

    // 공개 여부
    private Boolean isPublic;

    // 로그인한 관리자와 연결된 employee_id
    private Long writerEmployeeId;
}
