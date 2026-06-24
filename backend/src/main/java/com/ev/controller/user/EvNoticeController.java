package com.ev.controller.user;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ev.dto.notice.EvNoticeDTO;
import com.ev.service.user.EvNoticeService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/notice")
@RequiredArgsConstructor
public class EvNoticeController {

    private final EvNoticeService noticeService;

    // 메인 새소식 조회
    @GetMapping("/main")
    public List<EvNoticeDTO> mainNotice() {
        log.info("@# EvNoticeController.mainNotice()");
        return noticeService.getMainNoticeList();
    }

    // 공지사항 목록 조회
    @GetMapping("/list")
    public List<EvNoticeDTO> noticeList(
            @RequestParam(value = "keyword", required = false) String keyword) {
        log.info("@# EvNoticeController.noticeList()");
        log.info("@# keyword => {}", keyword);
        return noticeService.getNoticeList(keyword);
    }

    // 공지사항 검색
    @GetMapping("/search")
    public List<EvNoticeDTO> searchNotice(
            @RequestParam(value = "keyword", required = false) String keyword) {
        log.info("@# EvNoticeController.searchNotice()");
        log.info("@# keyword => {}", keyword);
        return noticeService.searchNotice(keyword);
    }

    // 공지사항 상세 조회
    @GetMapping("/{noticeId}")
    public ResponseEntity<EvNoticeDTO> noticeDetail(
            @PathVariable("noticeId") Long noticeId) {
        log.info("@# EvNoticeController.noticeDetail()");
        log.info("@# noticeId => {}", noticeId);

        EvNoticeDTO notice = noticeService.getNoticeDetail(noticeId);

        if (notice == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(notice);
    }
}
