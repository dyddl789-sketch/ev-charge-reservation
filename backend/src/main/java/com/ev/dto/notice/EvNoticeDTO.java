package com.ev.dto.notice;

import lombok.Data;

/*
 * 사용자 공지사항 화면 응답 DTO
 * React NoticeSection / NoticePage / NoticeDetailPage에서 사용하는 필드명과 맞춘다.
 */
@Data
public class EvNoticeDTO {

    // 공지사항 고유 번호
    private Long noticeId;

    // 작성자 직원 번호
    private Long writerId;

    // 작성자명
    private String writerName;

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

    // 조회수
    private Integer viewCount;

    // 화면 표시용 등록일 문자열 yyyy-MM-dd
    private String createdAt;

    // 화면 표시용 수정일 문자열 yyyy-MM-dd
    private String updatedAt;
}
