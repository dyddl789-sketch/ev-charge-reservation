package com.ev.controller.admin;

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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ev.dto.admin.employee.EvAdminEmployeeDTO;
import com.ev.dto.admin.fault.EvAdminActionCompleteRequestDTO;
import com.ev.dto.admin.fault.EvAdminFaultAssignRequestDTO;
import com.ev.dto.admin.fault.EvAdminFaultSearchDTO;
import com.ev.dto.admin.fault.EvAdminInspectionResultRequestDTO;
import com.ev.security.EvUserDetails;
import com.ev.service.admin.EvAdminFaultService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/*
 * 관리자 장애·점검관리 REST API Controller
 */
@Slf4j
@RestController
@RequestMapping("/admin/faults")
@RequiredArgsConstructor
public class EvAdminFaultController {

    private final EvAdminFaultService evAdminFaultService;

    @GetMapping
    public ResponseEntity<?> getFaultList(
            @AuthenticationPrincipal EvUserDetails userDetails,
            @RequestParam(value = "status", required = false) String status,
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "reportedFrom", required = false) String reportedFrom,
            @RequestParam(value = "reportedTo", required = false) String reportedTo,
            @RequestParam(value = "resolvedFrom", required = false) String resolvedFrom,
            @RequestParam(value = "resolvedTo", required = false) String resolvedTo,
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "size", defaultValue = "10") int size) {

        log.info("@# EvAdminFaultController.getFaultList()");
        log.info("@# status => {}, keyword => {}, reportedFrom => {}, reportedTo => {}, resolvedFrom => {}, resolvedTo => {}, page => {}, size => {}",
                status, keyword, reportedFrom, reportedTo, resolvedFrom, resolvedTo, page, size);

        if (userDetails == null) {
            return errorResponse(HttpStatus.UNAUTHORIZED, "로그인이 필요합니다.");
        }

        EvAdminFaultSearchDTO searchDTO = new EvAdminFaultSearchDTO();
        searchDTO.setStatus(status);
        searchDTO.setKeyword(keyword);
        searchDTO.setReportedFrom(reportedFrom);
        searchDTO.setReportedTo(reportedTo);
        searchDTO.setResolvedFrom(resolvedFrom);
        searchDTO.setResolvedTo(resolvedTo);
        searchDTO.setPage(page);
        searchDTO.setSize(size);

        try {
            return ResponseEntity.ok(evAdminFaultService.getFaultPage(userDetails.getMemberId(), searchDTO));
        } catch (IllegalArgumentException e) {
            log.warn("@# get fault list failed => {}", e.getMessage());
            return errorResponse(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    @GetMapping("/{faultId}")
    public ResponseEntity<?> getFaultDetail(@PathVariable("faultId") Long faultId) {
        log.info("@# EvAdminFaultController.getFaultDetail()");
        log.info("@# faultId => {}", faultId);

        try {
            return ResponseEntity.ok(evAdminFaultService.getFaultDetail(faultId));
        } catch (IllegalArgumentException e) {
            log.warn("@# get fault detail failed => {}", e.getMessage());
            return errorResponse(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }

    @GetMapping("/engineers")
    public ResponseEntity<List<EvAdminEmployeeDTO>> getEngineerList() {
        log.info("@# EvAdminFaultController.getEngineerList()");
        return ResponseEntity.ok(evAdminFaultService.getEngineerList());
    }

    @PostMapping("/{faultId}/assign")
    public ResponseEntity<?> assignFault(
            @AuthenticationPrincipal EvUserDetails userDetails,
            @PathVariable("faultId") Long faultId,
            @RequestBody EvAdminFaultAssignRequestDTO requestDTO) {

        log.info("@# EvAdminFaultController.assignFault()");
        log.info("@# faultId => {}, requestDTO => {}", faultId, requestDTO);

        if (userDetails == null) {
            return errorResponse(HttpStatus.UNAUTHORIZED, "로그인이 필요합니다.");
        }

        try {
            return ResponseEntity.ok(evAdminFaultService.assignFault(userDetails.getMemberId(), faultId, requestDTO));
        } catch (IllegalArgumentException e) {
            log.warn("@# assign fault failed => {}", e.getMessage());
            return errorResponse(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    @PostMapping("/{faultId}/cancel")
    public ResponseEntity<?> cancelFault(
            @AuthenticationPrincipal EvUserDetails userDetails,
            @PathVariable("faultId") Long faultId) {

        log.info("@# EvAdminFaultController.cancelFault()");
        log.info("@# faultId => {}", faultId);

        if (userDetails == null) {
            return errorResponse(HttpStatus.UNAUTHORIZED, "로그인이 필요합니다.");
        }

        try {
            return ResponseEntity.ok(evAdminFaultService.cancelFault(userDetails.getMemberId(), faultId));
        } catch (IllegalArgumentException e) {
            log.warn("@# cancel fault failed => {}", e.getMessage());
            return errorResponse(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    @PostMapping("/{faultId}/inspection/start")
    public ResponseEntity<?> startInspection(
            @AuthenticationPrincipal EvUserDetails userDetails,
            @PathVariable("faultId") Long faultId) {

        log.info("@# EvAdminFaultController.startInspection()");
        log.info("@# faultId => {}", faultId);

        if (userDetails == null) {
            return errorResponse(HttpStatus.UNAUTHORIZED, "로그인이 필요합니다.");
        }

        try {
            return ResponseEntity.ok(evAdminFaultService.startInspection(userDetails.getMemberId(), faultId));
        } catch (IllegalArgumentException e) {
            log.warn("@# start inspection failed => {}", e.getMessage());
            return errorResponse(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    @PostMapping("/{faultId}/inspection/result")
    public ResponseEntity<?> saveInspectionResult(
            @AuthenticationPrincipal EvUserDetails userDetails,
            @PathVariable("faultId") Long faultId,
            @RequestBody EvAdminInspectionResultRequestDTO requestDTO) {

        log.info("@# EvAdminFaultController.saveInspectionResult()");
        log.info("@# faultId => {}, requestDTO => {}", faultId, requestDTO);

        if (userDetails == null) {
            return errorResponse(HttpStatus.UNAUTHORIZED, "로그인이 필요합니다.");
        }

        try {
            return ResponseEntity.ok(evAdminFaultService.saveInspectionResult(userDetails.getMemberId(), faultId, requestDTO));
        } catch (IllegalArgumentException e) {
            log.warn("@# save inspection result failed => {}", e.getMessage());
            return errorResponse(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    @PostMapping("/{faultId}/action/complete")
    public ResponseEntity<?> completeAction(
            @AuthenticationPrincipal EvUserDetails userDetails,
            @PathVariable("faultId") Long faultId,
            @RequestBody EvAdminActionCompleteRequestDTO requestDTO) {

        log.info("@# EvAdminFaultController.completeAction()");
        log.info("@# faultId => {}, requestDTO => {}", faultId, requestDTO);

        if (userDetails == null) {
            return errorResponse(HttpStatus.UNAUTHORIZED, "로그인이 필요합니다.");
        }

        try {
            return ResponseEntity.ok(evAdminFaultService.completeAction(userDetails.getMemberId(), faultId, requestDTO));
        } catch (IllegalArgumentException e) {
            log.warn("@# complete action failed => {}", e.getMessage());
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
