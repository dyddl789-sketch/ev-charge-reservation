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

 // 회원가입 처리
    @PostMapping("/join")
    public String joinProcess(
            EvMemberDTO memberDTO,
            @RequestParam("emailId") String emailId,
            @RequestParam("emailDomain") String emailDomain,
            @RequestParam("emailDomainDirect") String emailDomainDirect,
            @RequestParam("profileImage") MultipartFile profileImage,
            RedirectAttributes rttr) {

        log.info("@# EvMemberController.joinProcess()");
        log.info("@# memberDTO => {}", memberDTO);

        // 프로필 이미지 업로드 확인용 로그
        log.info("@# profileImage empty => {}", profileImage.isEmpty());
        log.info("@# profileImage originalName => {}", profileImage.getOriginalFilename());
        log.info("@# profileImage size => {}", profileImage.getSize());

        try {
            String domain = "direct".equals(emailDomain)
                    ? emailDomainDirect
                    : emailDomain;

            memberDTO.setEmail(emailId + "@" + domain);

            evMemberService.join(memberDTO, profileImage);

            rttr.addFlashAttribute("msg", "회원가입이 완료되었습니다.");
            return "redirect:/login";

        } catch (IllegalArgumentException e) {
            rttr.addFlashAttribute("errorMsg", e.getMessage());
            return "redirect:/member/join";

        } catch (Exception e) {
            log.error("@# 회원가입 처리 중 오류", e);
            rttr.addFlashAttribute("errorMsg", "회원가입 처리 중 오류가 발생했습니다.");
            return "redirect:/member/join";
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

    // 회원정보 수정 처리
    @PostMapping("/mypage/edit")
    public String editProcess(
            EvMemberUpdateDTO updateDTO,
            @RequestParam("profileImage") MultipartFile profileImage,
            @AuthenticationPrincipal EvUserDetails userDetails,
            RedirectAttributes rttr) {

        log.info("@# EvMemberController.editProcess()");

        try {
            updateDTO.setMemberId(userDetails.getMemberId());

            evMemberService.updateMember(updateDTO, profileImage);

            EvMemberDTO updatedMember =
                    evMemberService.findByMemberId(userDetails.getMemberId());

            // Security 로그인 정보 즉시 갱신
            EvUserDetails updatedUserDetails =
                    new EvUserDetails(updatedMember);

            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(
                            updatedUserDetails,
                            null,
                            updatedUserDetails.getAuthorities()
                    );

            SecurityContextHolder.getContext().setAuthentication(authentication);

            rttr.addFlashAttribute("msg", "회원정보가 수정되었습니다.");

            return "redirect:/member/mypage/edit";

        } catch (IllegalArgumentException e) {
            rttr.addFlashAttribute("errorMsg", e.getMessage());
            return "redirect:/member/mypage/edit";

        } catch (Exception e) {
            log.error("@# 회원정보 수정 오류", e);
            rttr.addFlashAttribute("errorMsg", "회원정보 수정 중 오류가 발생했습니다.");
            return "redirect:/member/mypage/edit";
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
}