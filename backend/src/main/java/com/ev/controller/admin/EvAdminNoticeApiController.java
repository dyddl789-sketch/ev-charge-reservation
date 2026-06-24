package com.ev.controller.admin;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ev.dto.notice.EvNoticeDTO;
import com.ev.dto.notice.EvNoticeRequestDTO;
import com.ev.security.EvUserDetails;
import com.ev.service.user.EvNoticeService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/admin/notice")
@RequiredArgsConstructor
public class EvAdminNoticeApiController {

    private final EvNoticeService noticeService;

    // 관리자 공지사항 목록 조회
    @GetMapping("/list")
    public List<EvNoticeDTO> adminNoticeList(
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "status", required = false) String status) {
        log.info("@# EvAdminNoticeApiController.adminNoticeList()");
        log.info("@# keyword => {}", keyword);
        log.info("@# status => {}", status);
        return noticeService.getAdminNoticeList(keyword, status);
    }

    // 관리자 공지사항 상세 조회
    @GetMapping("/{noticeId}")
    public ResponseEntity<EvNoticeDTO> adminNoticeDetail(
            @PathVariable("noticeId") Long noticeId) {
        log.info("@# EvAdminNoticeApiController.adminNoticeDetail()");
        log.info("@# noticeId => {}", noticeId);

        EvNoticeDTO notice = noticeService.getAdminNoticeDetail(noticeId);

        if (notice == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(notice);
    }

    // 관리자 공지사항 등록
    @PostMapping
    public ResponseEntity<?> createNotice(
            @AuthenticationPrincipal EvUserDetails userDetails,
            @RequestBody EvNoticeRequestDTO requestDTO) {
        log.info("@# EvAdminNoticeApiController.createNotice()");

        if (userDetails == null) {
            return ResponseEntity.status(401).body(Map.of("message", "로그인이 필요합니다."));
        }

        try {
            EvNoticeDTO notice = noticeService.createNotice(userDetails.getMemberId(), requestDTO);

            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", "공지사항이 등록되었습니다.");
            result.put("notice", notice);

            return ResponseEntity.ok(result);
        } catch (IllegalArgumentException e) {
            log.warn("@# 공지사항 등록 실패 => {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ));
        }
    }

    // 관리자 공지사항 수정
    @PutMapping("/{noticeId}")
    public ResponseEntity<?> updateNotice(
            @PathVariable("noticeId") Long noticeId,
            @RequestBody EvNoticeRequestDTO requestDTO) {
        log.info("@# EvAdminNoticeApiController.updateNotice()");
        log.info("@# noticeId => {}", noticeId);

        try {
            EvNoticeDTO notice = noticeService.updateNotice(noticeId, requestDTO);

            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", "공지사항이 수정되었습니다.");
            result.put("notice", notice);

            return ResponseEntity.ok(result);
        } catch (IllegalArgumentException e) {
            log.warn("@# 공지사항 수정 실패 => {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ));
        }
    }

    // 관리자 공지사항 삭제 처리
    @DeleteMapping("/{noticeId}")
    public ResponseEntity<?> deleteNotice(
            @PathVariable("noticeId") Long noticeId) {
        log.info("@# EvAdminNoticeApiController.deleteNotice()");
        log.info("@# noticeId => {}", noticeId);

        try {
            noticeService.deleteNotice(noticeId);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "공지사항이 삭제 처리되었습니다."
            ));
        } catch (IllegalArgumentException e) {
            log.warn("@# 공지사항 삭제 실패 => {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ));
        }
    }
}
