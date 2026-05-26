package com.ev.controller.user;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.ev.dto.member.MemberDTO;
import com.ev.service.user.MemberService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller
@RequestMapping("/member")
@RequiredArgsConstructor
public class EvMemberController {

//	@RequiredArgsConstructor : 1. 불변성: final 필드를 사용하여 객체의 불변성을 보장할 수 있습니다.
//							   2. 테스트 용이성: 의존성을 명시적으로 선언하여 단위 테스트가 쉬워집니다.
//	 						   3. 순환 의존성 탐지: 컴파일 시점에 순환 의존성을 쉽게 발견할 수 있습니다.
    
    private final MemberService memberService;

    
//    회원가입 화면 이동
    
    @GetMapping("/join")
    public String join() {
        log.info("@# EvMemberController.join()");

        return "user/member/join";
    }

    
//    회원가입 처리
    @PostMapping("/join")
    public String joinProcess(MemberDTO memberDTO, RedirectAttributes rttr) {
        log.info("@# EvMemberController.joinProcess()");
        log.info("@# memberDTO => {}", memberDTO);

        try {
            
            memberService.join(memberDTO);

            
            rttr.addFlashAttribute("msg", "회원가입이 완료되었습니다.");

            
            return "redirect:/login";

        } catch (IllegalArgumentException e) {

            rttr.addFlashAttribute("errorMsg", e.getMessage());

            return "redirect:/member/join";
        }
    }


}





