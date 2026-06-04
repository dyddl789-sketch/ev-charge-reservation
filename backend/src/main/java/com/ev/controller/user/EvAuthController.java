package com.ev.controller.user;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestParam;
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
	public String loginForm(
	        @RequestParam(value = "error", required = false) String error,
	        Model model) {

	    log.info("@# EvAuthController.loginForm()");

	    if (error != null) {
	        model.addAttribute("errorMsg", error);
	    }

	    return "user/member/login";
	}
}