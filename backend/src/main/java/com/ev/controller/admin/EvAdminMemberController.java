package com.ev.controller.admin;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ev.dto.admin.EvAdminMemberPageDTO;
import com.ev.dto.admin.EvAdminMemberSearchDTO;
import com.ev.service.admin.EvAdminMemberService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/admin/member")
@RequiredArgsConstructor
public class EvAdminMemberController {

    private final EvAdminMemberService evAdminMemberService;

    // 회원 목록 JSON API
    @GetMapping("/list")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','OPERATOR')")
    public EvAdminMemberPageDTO memberList(
            @RequestParam(value = "status", required = false) String status,
            @RequestParam(value = "userType", required = false) String userType,
            @RequestParam(value = "joinStart", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate joinStart,
            @RequestParam(value = "joinEnd", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate joinEnd,
            @RequestParam(value = "searchType", required = false) String searchType,
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "page", required = false, defaultValue = "1") Integer page,
            @RequestParam(value = "size", required = false, defaultValue = "20") Integer size) {

        log.info("@# EvAdminMemberController.memberList()");
        log.info("@# status => {}, userType => {}, keyword => {}, page => {}, size => {}",
                status, userType, keyword, page, size);

        EvAdminMemberSearchDTO searchDTO = new EvAdminMemberSearchDTO();
        searchDTO.setStatus(status);
        searchDTO.setUserType(userType);
        searchDTO.setJoinStart(joinStart);
        searchDTO.setJoinEnd(joinEnd);
        searchDTO.setSearchType(searchType);
        searchDTO.setKeyword(keyword);
        searchDTO.setPage(page);
        searchDTO.setSize(size);

        return evAdminMemberService.getMemberPage(searchDTO);
    }

    // 회원 상세 JSON API
    @GetMapping("/detail")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','OPERATOR')")
    public Map<String, Object> memberDetail(@RequestParam("memberId") Long memberId) {

        log.info("@# EvAdminMemberController.memberDetail()");
        log.info("@# memberId => {}", memberId);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("memberDetail", evAdminMemberService.getMemberDetail(memberId));
        result.put("vehicleList", evAdminMemberService.getMemberVehicleList(memberId));
        result.put("reservationList", evAdminMemberService.getMemberReservationList(memberId));
        result.put("chargingList", evAdminMemberService.getMemberChargingList(memberId));
        result.put("paymentList", evAdminMemberService.getMemberPaymentList(memberId));

        return result;
    }

    // 회원 탈퇴 처리 JSON API
    @PostMapping("/withdraw")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public Map<String, Object> withdrawMember(
            @RequestBody(required = false) Map<String, Object> body,
            @RequestParam(value = "memberId", required = false) Long requestParamMemberId) {

        Long memberId = resolveLong(body, requestParamMemberId, "memberId");

        log.info("@# EvAdminMemberController.withdrawMember()");
        log.info("@# memberId => {}", memberId);

        evAdminMemberService.withdrawMember(memberId);

        return Map.of(
                "success", true,
                "message", "회원이 비활성화되었습니다.",
                "memberId", memberId
        );
    }

    // 회원 복구 처리 JSON API
    @PostMapping("/restore")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public Map<String, Object> restoreMember(
            @RequestBody(required = false) Map<String, Object> body,
            @RequestParam(value = "memberId", required = false) Long requestParamMemberId) {

        Long memberId = resolveLong(body, requestParamMemberId, "memberId");

        log.info("@# EvAdminMemberController.restoreMember()");
        log.info("@# memberId => {}", memberId);

        evAdminMemberService.restoreMember(memberId);

        return Map.of(
                "success", true,
                "message", "회원이 활성화되었습니다.",
                "memberId", memberId
        );
    }

    private Long resolveLong(Map<String, Object> body, Long requestParamValue, String key) {
        if (requestParamValue != null) {
            return requestParamValue;
        }

        if (body == null || body.get(key) == null) {
            throw new IllegalArgumentException(key + " 값이 필요합니다.");
        }

        Object value = body.get(key);
        if (value instanceof Number number) {
            return number.longValue();
        }

        return Long.valueOf(String.valueOf(value));
    }
}
