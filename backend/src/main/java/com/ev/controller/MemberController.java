package com.ev.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/member")
public class MemberController {
	
	// 로그인 화면
    @GetMapping("/login")
    public String loginPage() {
        return "member/login";
    }

    // 회원가입 화면
    @GetMapping("/join")
    public String joinPage() {
        return "member/join";
    }
}
