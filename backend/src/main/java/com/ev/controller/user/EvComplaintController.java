package com.ev.controller.user;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ev.dto.complaint.EvComplaintDTO;
import com.ev.dto.complaint.EvComplaintRequestDTO;
import com.ev.security.EvUserDetails;
import com.ev.service.user.EvComplaintService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/*
 * 사용자 민원 REST API Controller
 */
@Slf4j
@RestController
@RequiredArgsConstructor
public class EvComplaintController {

    private final EvComplaintService evComplaintService;

    /*
     * 민원 등록
     * POST /complaint
     */
    @PostMapping({"/complaint", "/complaints"})
    public ResponseEntity<Map<String, Object>> createComplaint(
            @AuthenticationPrincipal EvUserDetails userDetails,
            @RequestBody EvComplaintRequestDTO requestDTO) {

        log.info("@# EvComplaintController.createComplaint()");
        log.info("@# requestDTO => {}", requestDTO);

        if (userDetails == null) {
            return loginRequiredResponse();
        }

        try {
            Long complaintId = evComplaintService.createComplaint(userDetails.getMemberId(), requestDTO);

            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("complaintId", complaintId);
            result.put("message", "민원이 정상적으로 접수되었습니다.");

            return ResponseEntity.ok(result);

        } catch (IllegalArgumentException e) {
            log.warn("@# complaint create failed => {}", e.getMessage());

            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("message", e.getMessage());

            return ResponseEntity.badRequest().body(result);
        }
    }

    /*
     * 내 민원 목록
     * GET /complaints/my
     */
    @GetMapping("/complaints/my")
    public ResponseEntity<?> getMyComplaintList(@AuthenticationPrincipal EvUserDetails userDetails) {

        log.info("@# EvComplaintController.getMyComplaintList()");

        if (userDetails == null) {
            return loginRequiredResponse();
        }

        List<EvComplaintDTO> complaintList = evComplaintService.getMyComplaintList(userDetails.getMemberId());

        log.info("@# my complaintList size => {}", complaintList.size());

        return ResponseEntity.ok(complaintList);
    }

    /*
     * 내 민원 상세
     * GET /complaints/my/{complaintId}
     */
    @GetMapping("/complaints/my/{complaintId}")
    public ResponseEntity<?> getMyComplaintDetail(
            @AuthenticationPrincipal EvUserDetails userDetails,
            @PathVariable("complaintId") Long complaintId) {

        log.info("@# EvComplaintController.getMyComplaintDetail()");
        log.info("@# complaintId => {}", complaintId);

        if (userDetails == null) {
            return loginRequiredResponse();
        }

        try {
            EvComplaintDTO complaintDTO =
                    evComplaintService.getMyComplaintDetail(userDetails.getMemberId(), complaintId);

            return ResponseEntity.ok(complaintDTO);

        } catch (IllegalArgumentException e) {
            log.warn("@# complaint detail failed => {}", e.getMessage());

            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("message", e.getMessage());

            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(result);
        }
    }

    private ResponseEntity<Map<String, Object>> loginRequiredResponse() {
        Map<String, Object> result = new HashMap<>();
        result.put("success", false);
        result.put("message", "로그인이 필요합니다.");

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(result);
    }
}
