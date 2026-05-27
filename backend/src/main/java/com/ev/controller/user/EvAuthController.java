package com.ev.controller.user;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller
public class EvAuthController {

    /*
     * 로그인 화면 이동
     *
     * 실제 로그인 처리는 Spring Security에서 처리한다.
     * - 일반 로그인: formLogin
     * - 소셜 로그인: oauth2Login
     */
    @GetMapping("/login")
    public String loginForm() {
        log.info("@# EvAuthController.loginForm()");

        return "user/member/login";
    }
}