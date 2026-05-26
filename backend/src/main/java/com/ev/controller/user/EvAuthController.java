package com.ev.controller.user;


import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.ev.dto.member.MemberDTO;
import com.ev.service.user.MemberService;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 로그인 / 로그아웃을 담당하는 Controller
 *
 * 로그인은 일반 사용자와 관리자 모두 사용하는 공통 인증 기능이다.
 * 따라서 EvMemberController가 아니라 EvAuthController로 분리한다.
 */
@Slf4j
@Controller
@RequiredArgsConstructor
public class EvAuthController {

    private final MemberService memberService;

    
    @GetMapping("/login")
    public String loginForm() {
        log.info("@# EvAuthController.loginForm()");

        return "user/member/login";
    }

    /*
     * 로그인 처리
     * 처리 순서:
     * 1. 아이디로 회원 조회
     * 2. 비밀번호 비교
     * 3. 세션 저장
     * 4. userType에 따라 이동
     */
    
    @PostMapping("/login")
    public String loginProcess(@RequestParam("userId") String userId,
                               @RequestParam("password") String password,
                               @RequestParam("userType") String userType,
                               HttpSession session,
                               RedirectAttributes rttr) {
        log.info("@# EvAuthController.loginProcess()");
        log.info("@# userId => {}", userId);

        try {
            MemberDTO loginMember = memberService.login(userId, password, userType);

            /*
             * 로그인 성공 시 세션에 필요한 정보 저장
             *
             * 세션은 로그인 상태를 유지하기 위해 사용한다.
             */
            session.setAttribute("loginMemberId", loginMember.getMemberId());
            session.setAttribute("loginUserId", loginMember.getUserId());
            session.setAttribute("loginMemberName", loginMember.getMemberName());
            session.setAttribute("loginUserType", loginMember.getUserType());
            
            log.info("@# session loginMemberId => {}", session.getAttribute("loginMemberId"));
            log.info("@# session loginUserId => {}", session.getAttribute("loginUserId"));
            log.info("@# session loginMemberName => {}", session.getAttribute("loginMemberName"));
            log.info("@# session loginUserType => {}", session.getAttribute("loginUserType"));
            
            /*
             * 관리자와 일반 사용자 이동 경로 분리
             */
            if ("ADMIN".equals(loginMember.getUserType())) {
                return "redirect:/admin/dashboard";
            }

            return "redirect:/main";

        } catch (IllegalArgumentException e) {
            rttr.addFlashAttribute("errorMsg", e.getMessage());
            return "redirect:/login";
        }
        
        
    }

    /*
     * 로그아웃 처리
     *
     *
     * 세션을 제거하고 로그인 화면으로 이동한다.
     */
    @GetMapping("/logout")
    public String logout(HttpSession session, RedirectAttributes rttr) {
        log.info("@# EvAuthController.logout()");

        session.invalidate();

        rttr.addFlashAttribute("msg", "로그아웃되었습니다.");
        return "redirect:/login";
    }
}