package com.ev.security;

import java.io.IOException;

import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class EvLoginSuccessHandler implements AuthenticationSuccessHandler {

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication)
            throws IOException, ServletException {

        EvUserDetails userDetails =
                (EvUserDetails) authentication.getPrincipal();

        log.info("@# login success");
        log.info("@# userId => {}", userDetails.getUsername());
        log.info("@# userType => {}", userDetails.getUserType());

        // 관리자 로그인
        if ("ADMIN".equals(userDetails.getUserType())) {

            response.sendRedirect("/admin/dashboard");
            return;
        }

        // 일반 회원 로그인
        response.sendRedirect("/main");
    }
}