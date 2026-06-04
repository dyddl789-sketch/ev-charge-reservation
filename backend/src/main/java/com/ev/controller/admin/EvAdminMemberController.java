package com.ev.controller.admin;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import java.time.LocalDate;

import com.ev.dto.admin.EvAdminMemberPageDTO;
import com.ev.dto.admin.EvAdminMemberSearchDTO;
import com.ev.service.admin.EvAdminMemberService;
import com.ev.dto.admin.EvAdminMemberDetailDTO;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller
@RequestMapping("/admin/member")
@RequiredArgsConstructor
public class EvAdminMemberController {

    private final EvAdminMemberService evAdminMemberService;

    // 회원 목록 화면
    @GetMapping("/list")
    public String memberList(
            @RequestParam(value = "status", required = false) String status,
            @RequestParam(value = "joinStart", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate joinStart,
            @RequestParam(value = "joinEnd", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate joinEnd,
            @RequestParam(value = "searchType", required = false) String searchType,
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "page", required = false, defaultValue = "1") Integer page,
            @RequestParam(value = "size", required = false, defaultValue = "10") Integer size,
            Model model) {

        log.info("@# EvAdminMemberController.memberList()");

        EvAdminMemberSearchDTO searchDTO = new EvAdminMemberSearchDTO();

        searchDTO.setStatus(status);
        searchDTO.setJoinStart(joinStart);
        searchDTO.setJoinEnd(joinEnd);
        searchDTO.setSearchType(searchType);
        searchDTO.setKeyword(keyword);
        searchDTO.setPage(page);
        searchDTO.setSize(size);

        EvAdminMemberPageDTO memberPage =
                evAdminMemberService.getMemberPage(searchDTO);

        model.addAttribute("searchDTO", searchDTO);
        model.addAttribute("memberPage", memberPage);

        return "admin/member/member_list";
    }

    // 회원 탈퇴 처리
    @PostMapping("/withdraw")
    public String withdrawMember(
            @RequestParam("memberId") Long memberId) {

        log.info("@# EvAdminMemberController.withdrawMember()");
        log.info("@# memberId => {}", memberId);

        evAdminMemberService.withdrawMember(memberId);

        return "redirect:/admin/member/list";
    }

    // 회원 복구 처리
    @PostMapping("/restore")
    public String restoreMember(
            @RequestParam("memberId") Long memberId) {

        log.info("@# EvAdminMemberController.restoreMember()");
        log.info("@# memberId => {}", memberId);

        evAdminMemberService.restoreMember(memberId);

        return "redirect:/admin/member/list";
    }
    
 // 회원 상세보기
    @GetMapping("/detail")
    public String memberDetail(
            @RequestParam("memberId") Long memberId,
            Model model) {

        log.info("@# EvAdminMemberController.memberDetail()");
        log.info("@# memberId => {}", memberId);

        model.addAttribute("memberDetail",
                evAdminMemberService.getMemberDetail(memberId));

        model.addAttribute("vehicleList",
                evAdminMemberService.getMemberVehicleList(memberId));

        model.addAttribute("reservationList",
                evAdminMemberService.getMemberReservationList(memberId));

        model.addAttribute("chargingList",
                evAdminMemberService.getMemberChargingList(memberId));

        model.addAttribute("paymentList",
                evAdminMemberService.getMemberPaymentList(memberId));

        return "admin/member/member_detail";
    }
}