package com.ev.security;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class EvOAuth2JwtSuccessHandler implements AuthenticationSuccessHandler {

    private final EvJwtTokenProvider jwtTokenProvider;
    private final EvJwtRedisTokenService jwtRedisTokenService;

    @Value("${app.frontend-url:http://localhost:5173}")
    private String frontendUrl;

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication) throws IOException, ServletException {

        log.info("@# EvOAuth2JwtSuccessHandler.onAuthenticationSuccess()");

        EvUserDetails userDetails = (EvUserDetails) authentication.getPrincipal();

        log.info("@# OAuth2 로그인 성공 memberId => {}", userDetails.getMemberId());
        log.info("@# OAuth2 로그인 성공 userId => {}", userDetails.getUsername());
        log.info("@# OAuth2 로그인 성공 userType => {}", userDetails.getUserType());

        String accessToken = jwtTokenProvider.createAccessToken(userDetails);
        String refreshToken = jwtTokenProvider.createRefreshToken(userDetails);

        log.info("@# OAuth2 AccessToken 생성 => {}", accessToken);
        log.info("@# OAuth2 RefreshToken 생성 => {}", refreshToken);

        jwtRedisTokenService.saveRefreshToken(
                userDetails.getMemberId(),
                refreshToken,
                jwtTokenProvider.getRefreshTokenExpirationMs()
        );

        log.info("@# OAuth2 Redis RefreshToken 저장 완료 memberId => {}", userDetails.getMemberId());

        String redirectUrl = frontendUrl
                + "/auth/oauth2/redirect"
                + "?accessToken=" + URLEncoder.encode(accessToken, StandardCharsets.UTF_8)
                + "&refreshToken=" + URLEncoder.encode(refreshToken, StandardCharsets.UTF_8)
                + "&userType=" + URLEncoder.encode(userDetails.getUserType(), StandardCharsets.UTF_8);

        log.info("@# OAuth2 redirectUrl => {}", redirectUrl);

        response.sendRedirect(redirectUrl);
    }
}