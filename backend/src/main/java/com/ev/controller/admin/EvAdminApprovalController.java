package com.ev.controller.admin;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ev.dto.admin.approval.EvAdminApprovalDTO;
import com.ev.dto.admin.approval.EvAdminApprovalDecisionRequestDTO;
import com.ev.dto.admin.approval.EvAdminApprovalSubmitRequestDTO;
import com.ev.security.EvUserDetails;
import com.ev.service.admin.EvAdminApprovalService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/*
 * 관리자 전자결재 REST API Controller
 */
@Slf4j
@RestController
@RequestMapping("/admin/approvals")
@RequiredArgsConstructor
public class EvAdminApprovalController {

    private final EvAdminApprovalService evAdminApprovalService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','ENGINEER')")
    public ResponseEntity<?> getApprovalList(
            @AuthenticationPrincipal EvUserDetails userDetails,
            @RequestParam(value = "box", required = false) String box,
            @RequestParam(value = "status", required = false) String status,
            @RequestParam(value = "keyword", required = false) String keyword) {

        log.info("@# EvAdminApprovalController.getApprovalList()");
        log.info("@# box => {}, status => {}, keyword => {}", box, status, keyword);

        if (userDetails == null) {
            return errorResponse(HttpStatus.UNAUTHORIZED, "로그인이 필요합니다.");
        }

        try {
            List<EvAdminApprovalDTO> approvalList = evAdminApprovalService.getApprovalList(
                    userDetails.getMemberId(),
                    userDetails.getUserType(),
                    box,
                    status,
                    keyword
            );
            return ResponseEntity.ok(approvalList);
        } catch (IllegalArgumentException e) {
            log.warn("@# get approval list failed => {}", e.getMessage());
            return errorResponse(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    @GetMapping("/{documentId}")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','ENGINEER')")
    public ResponseEntity<?> getApprovalDetail(@PathVariable("documentId") Long documentId) {
        log.info("@# EvAdminApprovalController.getApprovalDetail()");
        log.info("@# documentId => {}", documentId);

        try {
            return ResponseEntity.ok(evAdminApprovalService.getApprovalDetail(documentId));
        } catch (IllegalArgumentException e) {
            log.warn("@# get approval detail failed => {}", e.getMessage());
            return errorResponse(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }

    @PostMapping("/faults/{faultId}/submit")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','ENGINEER')")
    public ResponseEntity<?> submitFaultApproval(
            @AuthenticationPrincipal EvUserDetails userDetails,
            @PathVariable("faultId") Long faultId,
            @RequestBody EvAdminApprovalSubmitRequestDTO requestDTO) {

        log.info("@# EvAdminApprovalController.submitFaultApproval()");
        log.info("@# faultId => {}, requestDTO => {}", faultId, requestDTO);

        if (userDetails == null) {
            return errorResponse(HttpStatus.UNAUTHORIZED, "로그인이 필요합니다.");
        }

        try {
            return ResponseEntity.ok(evAdminApprovalService.submitFaultApproval(userDetails.getMemberId(), faultId, requestDTO));
        } catch (IllegalArgumentException e) {
            log.warn("@# submit fault approval failed => {}", e.getMessage());
            return errorResponse(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    @PostMapping("/{documentId}/approve")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ResponseEntity<?> approveApproval(
            @AuthenticationPrincipal EvUserDetails userDetails,
            @PathVariable("documentId") Long documentId,
            @RequestBody EvAdminApprovalDecisionRequestDTO requestDTO) {

        log.info("@# EvAdminApprovalController.approveApproval()");
        log.info("@# documentId => {}, requestDTO => {}", documentId, requestDTO);

        if (userDetails == null) {
            return errorResponse(HttpStatus.UNAUTHORIZED, "로그인이 필요합니다.");
        }

        try {
            return ResponseEntity.ok(evAdminApprovalService.approveApproval(userDetails.getMemberId(), userDetails.getUserType(), documentId, requestDTO));
        } catch (IllegalArgumentException e) {
            log.warn("@# approve approval failed => {}", e.getMessage());
            return errorResponse(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    @PostMapping("/{documentId}/reject")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ResponseEntity<?> rejectApproval(
            @AuthenticationPrincipal EvUserDetails userDetails,
            @PathVariable("documentId") Long documentId,
            @RequestBody EvAdminApprovalDecisionRequestDTO requestDTO) {

        log.info("@# EvAdminApprovalController.rejectApproval()");
        log.info("@# documentId => {}, requestDTO => {}", documentId, requestDTO);

        if (userDetails == null) {
            return errorResponse(HttpStatus.UNAUTHORIZED, "로그인이 필요합니다.");
        }

        try {
            return ResponseEntity.ok(evAdminApprovalService.rejectApproval(userDetails.getMemberId(), userDetails.getUserType(), documentId, requestDTO));
        } catch (IllegalArgumentException e) {
            log.warn("@# reject approval failed => {}", e.getMessage());
            return errorResponse(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    private ResponseEntity<Map<String, Object>> errorResponse(HttpStatus status, String message) {
        Map<String, Object> result = new HashMap<>();
        result.put("success", false);
        result.put("message", message);
        return ResponseEntity.status(status).body(result);
    }
}
