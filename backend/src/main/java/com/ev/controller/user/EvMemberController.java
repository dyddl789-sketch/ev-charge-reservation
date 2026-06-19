package com.ev.controller.user;

import java.util.Map;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.ev.dto.member.EvMemberDTO;
import com.ev.dto.member.EvMemberUpdateDTO;
import com.ev.security.EvUserDetails;
import com.ev.service.user.EvMemberService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller
@RequestMapping("/member")
@RequiredArgsConstructor
public class EvMemberController {

    private final EvMemberService evMemberService;

    // 회원가입 화면
    @GetMapping("/join")
    public String join() {
        log.info("@# EvMemberController.join()");
        return "user/member/join";
    }

    @PostMapping("/join")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> joinProcess(
            EvMemberDTO memberDTO,
            @RequestParam("emailId") String emailId,
            @RequestParam("emailDomain") String emailDomain,
            @RequestParam(value = "emailDomainDirect", required = false) String emailDomainDirect,
            @RequestParam(value = "profileImage", required = false) MultipartFile profileImage) {

        log.info("@# REST EvMemberController.joinProcess()");
        log.info("@# memberDTO => {}", memberDTO);

        try {
            String domain = "direct".equals(emailDomain)
                    ? emailDomainDirect
                    : emailDomain;

            memberDTO.setEmail(emailId + "@" + domain);

            evMemberService.join(memberDTO, profileImage);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "회원가입이 완료되었습니다."
            ));

        } catch (IllegalArgumentException e) {
            log.warn("@# 회원가입 검증 실패 => {}", e.getMessage());

            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ));

        } catch (Exception e) {
            log.error("@# 회원가입 처리 중 오류", e);

            return ResponseEntity.internalServerError().body(Map.of(
                    "success", false,
                    "message", "회원가입 처리 중 오류가 발생했습니다."
            ));
        }
    }

    // 아이디 중복확인
    @GetMapping("/check-user-id")
    @ResponseBody
    public Map<String, Boolean> checkUserId(@RequestParam("userId") String userId) {

        log.info("@# EvMemberController.checkUserId()");
        log.info("@# userId => {}", userId);

        boolean available = evMemberService.isUserIdAvailable(userId);

        return Map.of("available", available);
    }

    // 이메일 인증번호 발송
    @PostMapping("/email-code/send")
    @ResponseBody
    public Map<String, Object> sendEmailCode(@RequestParam("email") String email) {

        log.info("@# EvMemberController.sendEmailCode()");
        log.info("@# email => {}", email);

        try {
            evMemberService.sendEmailCode(email);

            return Map.of(
                    "success", true,
                    "message", "인증번호가 발송되었습니다."
            );

        } catch (IllegalArgumentException e) {
            return Map.of(
                    "success", false,
                    "message", e.getMessage()
            );

        } catch (Exception e) {
            log.error("@# 이메일 인증번호 발송 오류", e);

            return Map.of(
                    "success", false,
                    "message", "인증번호 발송 중 오류가 발생했습니다."
            );
        }
    }

    // 이메일 인증번호 확인
    @PostMapping("/email-code/verify")
    @ResponseBody
    public Map<String, Object> verifyEmailCode(
            @RequestParam("email") String email,
            @RequestParam("code") String code) {

        log.info("@# EvMemberController.verifyEmailCode()");
        log.info("@# email => {}", email);

        boolean verified = evMemberService.verifyEmailCode(email, code);

        if (verified) {
            return Map.of(
                    "success", true,
                    "message", "이메일 인증이 완료되었습니다."
            );
        }

        return Map.of(
                "success", false,
                "message", "인증번호가 일치하지 않거나 만료되었습니다."
        );
    }

    // 회원정보 수정 화면
    @GetMapping("/mypage/edit")
    public String editForm(
            @AuthenticationPrincipal EvUserDetails userDetails,
            Model model) {

        log.info("@# EvMemberController.editForm()");

        EvMemberDTO member =
                evMemberService.findByMemberId(userDetails.getMemberId());

        model.addAttribute("member", member);

        return "user/member/member_edit";
    }

    @PostMapping("/mypage/edit")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> editProcess(
            EvMemberUpdateDTO updateDTO,
            @RequestParam(value = "profileImage", required = false) MultipartFile profileImage,
            @AuthenticationPrincipal EvUserDetails userDetails) {

        log.info("@# REST EvMemberController.editProcess()");

        if (userDetails == null) {
            return ResponseEntity.status(401).body(Map.of(
                    "success", false,
                    "message", "로그인이 필요합니다."
            ));
        }

        try {
            updateDTO.setMemberId(userDetails.getMemberId());

            evMemberService.updateMember(updateDTO, profileImage);

            EvMemberDTO updatedMember =
                    evMemberService.findByMemberId(userDetails.getMemberId());

            EvUserDetails updatedUserDetails =
                    new EvUserDetails(updatedMember);

            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(
                            updatedUserDetails,
                            null,
                            updatedUserDetails.getAuthorities()
                    );

            SecurityContextHolder.getContext().setAuthentication(authentication);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "회원정보가 수정되었습니다."
            ));

        } catch (IllegalArgumentException e) {
            log.warn("@# 회원정보 수정 검증 실패 => {}", e.getMessage());

            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ));

        } catch (Exception e) {
            log.error("@# 회원정보 수정 오류", e);

            return ResponseEntity.internalServerError().body(Map.of(
                    "success", false,
                    "message", "회원정보 수정 중 오류가 발생했습니다."
            ));
        }
    }
    
 // 닉네임 중복확인
    @GetMapping("/check-nickname")
    @ResponseBody
    public Map<String, Boolean> checkNickname(
            @RequestParam("nickname") String nickname) {

        log.info("@# EvMemberController.checkNickname()");
        log.info("@# nickname => {}", nickname);

        boolean available =
                evMemberService.isNicknameAvailable(nickname);

        return Map.of("available", available);
    }

    // 이메일 중복확인
    @GetMapping("/check-email")
    @ResponseBody
    public Map<String, Boolean> checkEmail(
            @RequestParam("email") String email) {

        log.info("@# EvMemberController.checkEmail()");
        log.info("@# email => {}", email);

        boolean available =
                evMemberService.isEmailAvailable(email);

        return Map.of("available", available);
    }

    // 휴대폰 번호 중복확인
    @GetMapping("/check-phone")
    @ResponseBody
    public Map<String, Boolean> checkPhone(
            @RequestParam("phone") String phone) {

        log.info("@# EvMemberController.checkPhone()");
        log.info("@# phone => {}", phone);

        boolean available =
                evMemberService.isPhoneAvailable(phone);

        return Map.of("available", available);
    }
    
 // 회원탈퇴
    @DeleteMapping("/{memberId}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> deleteMember(
            @PathVariable("memberId") Long memberId,
            @AuthenticationPrincipal EvUserDetails userDetails) {

        log.info("@# REST EvMemberController.deleteMember()");
        log.info("@# memberId => {}", memberId);

        if (userDetails == null) {
            return ResponseEntity.status(401).body(Map.of(
                    "success", false,
                    "message", "로그인이 필요합니다."
            ));
        }

        if (!memberId.equals(userDetails.getMemberId())) {
            return ResponseEntity.status(403).body(Map.of(
                    "success", false,
                    "message", "본인 계정만 탈퇴할 수 있습니다."
            ));
        }

        evMemberService.deleteMember(memberId);

        SecurityContextHolder.clearContext();

        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "회원탈퇴가 완료되었습니다."
        ));
    }
}

