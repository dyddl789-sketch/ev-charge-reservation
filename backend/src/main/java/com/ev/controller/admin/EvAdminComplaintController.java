package com.ev.controller.admin;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ev.dto.complaint.EvComplaintAssignRequestDTO;
import com.ev.dto.complaint.EvComplaintDTO;
import com.ev.dto.complaint.EvComplaintSearchDTO;
import com.ev.dto.complaint.EvComplaintStatusRequestDTO;
import com.ev.security.EvUserDetails;
import com.ev.service.user.EvComplaintService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/*
 * 관리자 민원 관리 REST API Controller
 */
@Slf4j
@RestController
@RequestMapping("/admin/complaints")
@RequiredArgsConstructor
public class EvAdminComplaintController {

    private final EvComplaintService evComplaintService;

    /*
     * 관리자 민원 목록
     * GET /admin/complaints?status=&complaintType=&priority=&keyword=
     */
    @GetMapping
    public ResponseEntity<List<EvComplaintDTO>> getComplaintList(
            @RequestParam(value = "status", required = false) String status,
            @RequestParam(value = "complaintType", required = false) String complaintType,
            @RequestParam(value = "priority", required = false) String priority,
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "stationId", required = false) Long stationId,
            @RequestParam(value = "chargerId", required = false) Long chargerId) {

        log.info("@# EvAdminComplaintController.getComplaintList()");
        log.info("@# status => {}, complaintType => {}, priority => {}, keyword => {}, stationId => {}, chargerId => {}",
                status, complaintType, priority, keyword, stationId, chargerId);

        EvComplaintSearchDTO searchDTO = new EvComplaintSearchDTO();
        searchDTO.setStatus(status);
        searchDTO.setComplaintType(complaintType);
        searchDTO.setPriority(priority);
        searchDTO.setKeyword(keyword);
        searchDTO.setStationId(stationId);
        searchDTO.setChargerId(chargerId);

        List<EvComplaintDTO> complaintList = evComplaintService.getAdminComplaintList(searchDTO);

        log.info("@# admin complaintList size => {}", complaintList.size());

        return ResponseEntity.ok(complaintList);
    }

    /*
     * 관리자 민원 상세
     * GET /admin/complaints/{complaintId}
     */
    @GetMapping("/{complaintId}")
    public ResponseEntity<?> getComplaintDetail(@PathVariable("complaintId") Long complaintId) {

        log.info("@# EvAdminComplaintController.getComplaintDetail()");
        log.info("@# complaintId => {}", complaintId);

        try {
            EvComplaintDTO complaintDTO = evComplaintService.getAdminComplaintDetail(complaintId);
            return ResponseEntity.ok(complaintDTO);

        } catch (IllegalArgumentException e) {
            log.warn("@# admin complaint detail failed => {}", e.getMessage());
            return errorResponse(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }

    /*
     * 관리자 민원 상태 변경
     * PUT /admin/complaints/{complaintId}/status
     */
    @RequestMapping(value = "/{complaintId}/status", method = {RequestMethod.PUT, RequestMethod.PATCH})
    public ResponseEntity<Map<String, Object>> updateComplaintStatus(
            @AuthenticationPrincipal EvUserDetails userDetails,
            @PathVariable("complaintId") Long complaintId,
            @RequestBody EvComplaintStatusRequestDTO requestDTO) {

        log.info("@# EvAdminComplaintController.updateComplaintStatus()");
        log.info("@# complaintId => {}, requestDTO => {}", complaintId, requestDTO);

        if (userDetails == null) {
            return errorResponse(HttpStatus.UNAUTHORIZED, "로그인이 필요합니다.");
        }

        try {
            evComplaintService.updateComplaintStatus(userDetails.getMemberId(), complaintId, requestDTO);
            return successResponse("민원 상태가 변경되었습니다.");

        } catch (IllegalArgumentException e) {
            log.warn("@# update complaint status failed => {}", e.getMessage());
            return errorResponse(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    /*
     * 관리자 민원 담당자 배정
     * PUT /admin/complaints/{complaintId}/assign
     */
    @RequestMapping(value = "/{complaintId}/assign", method = {RequestMethod.PUT, RequestMethod.PATCH})
    public ResponseEntity<Map<String, Object>> assignComplaint(
            @AuthenticationPrincipal EvUserDetails userDetails,
            @PathVariable("complaintId") Long complaintId,
            @RequestBody EvComplaintAssignRequestDTO requestDTO) {

        log.info("@# EvAdminComplaintController.assignComplaint()");
        log.info("@# complaintId => {}, requestDTO => {}", complaintId, requestDTO);

        if (userDetails == null) {
            return errorResponse(HttpStatus.UNAUTHORIZED, "로그인이 필요합니다.");
        }

        try {
            evComplaintService.assignComplaint(userDetails.getMemberId(), complaintId, requestDTO);
            return successResponse("민원 담당자가 배정되었습니다.");

        } catch (IllegalArgumentException e) {
            log.warn("@# assign complaint failed => {}", e.getMessage());
            return errorResponse(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    private ResponseEntity<Map<String, Object>> successResponse(String message) {
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", message);
        return ResponseEntity.ok(result);
    }

    private ResponseEntity<Map<String, Object>> errorResponse(HttpStatus status, String message) {
        Map<String, Object> result = new HashMap<>();
        result.put("success", false);
        result.put("message", message);
        return ResponseEntity.status(status).body(result);
    }
}
