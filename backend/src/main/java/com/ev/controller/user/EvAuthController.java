package com.ev.controller.user;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller
public class EvAuthController {

    @GetMapping("/login")
    public String loginForm() {
        log.info("@# EvAuthController.loginForm()");

        return "user/member/login";
    }
}