package com.ev.service.user;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ev.dao.user.EvNoticeDAO;
import com.ev.dto.notice.EvNoticeDTO;
import com.ev.dto.notice.EvNoticeRequestDTO;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class EvNoticeServiceImpl implements EvNoticeService {

    private final EvNoticeDAO noticeDAO;

    // 메인 새소식 조회
    @Override
    public List<EvNoticeDTO> getMainNoticeList() {
        log.info("@# EvNoticeServiceImpl.getMainNoticeList()");
        return noticeDAO.findMainNoticeList(4);
    }

    // 사용자 공지사항 목록 조회
    @Override
    public List<EvNoticeDTO> getNoticeList(String keyword) {
        log.info("@# EvNoticeServiceImpl.getNoticeList()");
        log.info("@# keyword => {}", keyword);
        return noticeDAO.findPublishedNoticeList(keyword);
    }

    // 사용자 공지사항 검색
    @Override
    public List<EvNoticeDTO> searchNotice(String keyword) {
        log.info("@# EvNoticeServiceImpl.searchNotice()");
        log.info("@# keyword => {}", keyword);
        return noticeDAO.findPublishedNoticeList(keyword);
    }

    // 사용자 공지사항 상세 조회 + 조회수 증가
    @Override
    @Transactional
    public EvNoticeDTO getNoticeDetail(Long noticeId) {
        log.info("@# EvNoticeServiceImpl.getNoticeDetail()");
        log.info("@# noticeId => {}", noticeId);

        noticeDAO.increaseViewCount(noticeId);
        return noticeDAO.findPublishedNoticeById(noticeId);
    }

    // 관리자 공지사항 목록 조회
    @Override
    public List<EvNoticeDTO> getAdminNoticeList(String keyword, String status) {
        log.info("@# EvNoticeServiceImpl.getAdminNoticeList()");
        log.info("@# keyword => {}", keyword);
        log.info("@# status => {}", status);
        return noticeDAO.findAdminNoticeList(keyword, status);
    }

    // 관리자 공지사항 상세 조회
    @Override
    public EvNoticeDTO getAdminNoticeDetail(Long noticeId) {
        log.info("@# EvNoticeServiceImpl.getAdminNoticeDetail()");
        log.info("@# noticeId => {}", noticeId);
        return noticeDAO.findAdminNoticeById(noticeId);
    }

    // 관리자 공지사항 등록
    @Override
    @Transactional
    public EvNoticeDTO createNotice(Long memberId, EvNoticeRequestDTO requestDTO) {
        log.info("@# EvNoticeServiceImpl.createNotice()");
        log.info("@# memberId => {}", memberId);
        log.info("@# title => {}", requestDTO.getTitle());

        validateNoticeRequest(requestDTO);

        Long writerEmployeeId = null;
        if (memberId != null) {
            writerEmployeeId = noticeDAO.findEmployeeIdByMemberId(memberId);
        }

        log.info("@# writerEmployeeId => {}", writerEmployeeId);

        requestDTO.setWriterEmployeeId(writerEmployeeId);
        applyDefaultValue(requestDTO);

        noticeDAO.insertNotice(requestDTO);

        log.info("@# 등록된 noticeId => {}", requestDTO.getNoticeId());
        return noticeDAO.findAdminNoticeById(requestDTO.getNoticeId());
    }

    // 관리자 공지사항 수정
    @Override
    @Transactional
    public EvNoticeDTO updateNotice(Long noticeId, EvNoticeRequestDTO requestDTO) {
        log.info("@# EvNoticeServiceImpl.updateNotice()");
        log.info("@# noticeId => {}", noticeId);
        log.info("@# title => {}", requestDTO.getTitle());

        validateNoticeRequest(requestDTO);
        applyDefaultValue(requestDTO);

        int result = noticeDAO.updateNotice(noticeId, requestDTO);

        if (result == 0) {
            throw new IllegalArgumentException("수정할 공지사항을 찾을 수 없습니다.");
        }

        return noticeDAO.findAdminNoticeById(noticeId);
    }

    // 관리자 공지사항 삭제 처리
    @Override
    @Transactional
    public void deleteNotice(Long noticeId) {
        log.info("@# EvNoticeServiceImpl.deleteNotice()");
        log.info("@# noticeId => {}", noticeId);

        int result = noticeDAO.deleteNotice(noticeId);

        if (result == 0) {
            throw new IllegalArgumentException("삭제할 공지사항을 찾을 수 없습니다.");
        }
    }

    // 공지사항 필수값 검증
    private void validateNoticeRequest(EvNoticeRequestDTO requestDTO) {
        if (requestDTO == null) {
            throw new IllegalArgumentException("공지사항 데이터가 없습니다.");
        }

        if (requestDTO.getTitle() == null || requestDTO.getTitle().trim().isEmpty()) {
            throw new IllegalArgumentException("공지사항 제목을 입력해 주세요.");
        }

        if (requestDTO.getContent() == null || requestDTO.getContent().trim().isEmpty()) {
            throw new IllegalArgumentException("공지사항 내용을 입력해 주세요.");
        }
    }

    // 프론트에서 값을 보내지 않았을 때 기본값 적용
    private void applyDefaultValue(EvNoticeRequestDTO requestDTO) {
        if (requestDTO.getCategory() == null || requestDTO.getCategory().trim().isEmpty()) {
            requestDTO.setCategory("공지");
        }

        if (requestDTO.getStatus() == null || requestDTO.getStatus().trim().isEmpty()) {
            requestDTO.setStatus("게시");
        }

        if (requestDTO.getIsPinned() == null) {
            requestDTO.setIsPinned(false);
        }

        if (requestDTO.getIsPublic() == null) {
            requestDTO.setIsPublic(true);
        }
    }
}
