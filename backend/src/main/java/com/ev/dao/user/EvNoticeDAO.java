package com.ev.dao.user;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.ev.dto.notice.EvNoticeDTO;
import com.ev.dto.notice.EvNoticeRequestDTO;

@Mapper
public interface EvNoticeDAO {

    // 메인 화면 새소식 조회
    List<EvNoticeDTO> findMainNoticeList(@Param("limit") int limit);

    // 사용자 공지사항 목록 조회
    List<EvNoticeDTO> findPublishedNoticeList(@Param("keyword") String keyword);

    // 사용자 공지사항 상세 조회
    EvNoticeDTO findPublishedNoticeById(@Param("noticeId") Long noticeId);

    // 조회수 증가
    int increaseViewCount(@Param("noticeId") Long noticeId);

    // 관리자 공지사항 목록 조회
    List<EvNoticeDTO> findAdminNoticeList(@Param("keyword") String keyword,
                                          @Param("status") String status);

    // 관리자 공지사항 상세 조회
    EvNoticeDTO findAdminNoticeById(@Param("noticeId") Long noticeId);

    // 관리자 member_id에 연결된 employee_id 조회
    Long findEmployeeIdByMemberId(@Param("memberId") Long memberId);

    // 관리자 공지사항 등록
    int insertNotice(EvNoticeRequestDTO requestDTO);

    // 관리자 공지사항 수정
    int updateNotice(@Param("noticeId") Long noticeId,
                     @Param("request") EvNoticeRequestDTO requestDTO);

    // 관리자 공지사항 삭제 처리
    int deleteNotice(@Param("noticeId") Long noticeId);
}
