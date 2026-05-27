package com.ev.controller.user;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.ev.security.EvUserDetails;

import lombok.extern.slf4j.Slf4j;

@Controller
@Slf4j
public class EvMainController {

    @GetMapping("/main")
    public String main(
            @AuthenticationPrincipal EvUserDetails userDetails,
            Model model) {

        // 로그인 상태 확인
        if (userDetails != null) {

            log.info("@# 로그인 사용자 ID => {}",
                    userDetails.getUsername());

            log.info("@# 로그인 사용자 이름 => {}",
                    userDetails.getMemberName());

            log.info("@# 로그인 사용자 권한 => {}",
                    userDetails.getUserType());

            // JSP 전달
            model.addAttribute(
                    "loginMemberName",
                    userDetails.getMemberName()
            );
        }

        return "user/main/main";
    }
}