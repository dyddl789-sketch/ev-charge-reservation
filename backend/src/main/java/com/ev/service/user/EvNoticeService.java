package com.ev.service.user;

import java.util.List;

import com.ev.dto.notice.EvNoticeDTO;
import com.ev.dto.notice.EvNoticeRequestDTO;

public interface EvNoticeService {

    // 메인 새소식 조회
    List<EvNoticeDTO> getMainNoticeList();

    // 사용자 공지사항 목록 조회
    List<EvNoticeDTO> getNoticeList(String keyword);

    // 사용자 공지사항 검색
    List<EvNoticeDTO> searchNotice(String keyword);

    // 사용자 공지사항 상세 조회
    EvNoticeDTO getNoticeDetail(Long noticeId);

    // 관리자 공지사항 목록 조회
    List<EvNoticeDTO> getAdminNoticeList(String keyword, String status);

    // 관리자 공지사항 상세 조회
    EvNoticeDTO getAdminNoticeDetail(Long noticeId);

    // 관리자 공지사항 등록
    EvNoticeDTO createNotice(Long memberId, EvNoticeRequestDTO requestDTO);

    // 관리자 공지사항 수정
    EvNoticeDTO updateNotice(Long noticeId, EvNoticeRequestDTO requestDTO);

    // 관리자 공지사항 삭제 처리
    void deleteNotice(Long noticeId);
}
