package com.ev.security;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import com.ev.dao.user.EvMemberDAO;
import com.ev.dto.member.EvMemberDTO;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class EvLoginFailureHandler implements AuthenticationFailureHandler {

    private final EvMemberDAO evMemberDAO;

    @Override
    public void onAuthenticationFailure(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException exception)
            throws IOException, ServletException {

        log.info("@# EvLoginFailureHandler.onAuthenticationFailure()");

        String userId = request.getParameter("userId");
        log.info("@# login fail userId => {}", userId);

        String errorMessage;

        EvMemberDTO member = evMemberDAO.findByUserId(userId);

        if (member == null) {
            errorMessage = "존재하지 않는 아이디입니다.";
        } else {
            errorMessage = "비밀번호가 일치하지 않습니다.";
        }

        String encodedMessage =
                URLEncoder.encode(errorMessage, StandardCharsets.UTF_8);

        response.sendRedirect("/login?error=" + encodedMessage);
    }
}