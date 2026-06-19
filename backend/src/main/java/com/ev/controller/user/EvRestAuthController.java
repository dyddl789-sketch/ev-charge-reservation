package com.ev.controller.user;

import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ev.dto.auth.AuthLoginRequestDTO;
import com.ev.dto.auth.AuthLoginResponseDTO;
import com.ev.dto.auth.AuthLogoutRequestDTO;
import com.ev.dto.auth.AuthMeResponseDTO;
import com.ev.dto.auth.AuthRefreshRequestDTO;
import com.ev.dto.auth.AuthTokenResponseDTO;
import com.ev.dto.member.EvMemberDTO;
import com.ev.security.EvJwtRedisTokenService;
import com.ev.security.EvJwtTokenProvider;
import com.ev.security.EvUserDetails;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
public class EvRestAuthController {

    private final AuthenticationManager authenticationManager;
    private final EvJwtTokenProvider jwtTokenProvider;
    private final EvJwtRedisTokenService jwtRedisTokenService;

    // React 로그인 API
    @PostMapping("/login")
    public ResponseEntity<AuthLoginResponseDTO> login(
            @Valid @RequestBody AuthLoginRequestDTO requestDTO) {

        log.info("@# REST login 요청 userId => {}", requestDTO.getUserId());

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        requestDTO.getUserId(),
                        requestDTO.getPassword()
                )
        );

        log.info("@# authentication => {}", authentication);

        SecurityContextHolder.getContext().setAuthentication(authentication);

        EvUserDetails userDetails = (EvUserDetails) authentication.getPrincipal();
        EvMemberDTO member = userDetails.getEvMemberDTO();

        log.info("@# 로그인 인증 성공 memberId => {}", userDetails.getMemberId());
        log.info("@# 로그인 인증 성공 userId => {}", userDetails.getUsername());
        log.info("@# 로그인 인증 성공 userType => {}", member.getUserType());

        String accessToken = jwtTokenProvider.createAccessToken(userDetails);
        String refreshToken = jwtTokenProvider.createRefreshToken(userDetails);

        log.info("@# AccessToken 생성 => {}", accessToken);
        log.info("@# RefreshToken 생성 => {}", refreshToken);

        jwtRedisTokenService.saveRefreshToken(
                userDetails.getMemberId(),
                refreshToken,
                jwtTokenProvider.getRefreshTokenExpirationMs()
        );

        log.info("@# Redis RefreshToken 저장 완료 memberId => {}", userDetails.getMemberId());

        AuthLoginResponseDTO responseDTO = new AuthLoginResponseDTO();
        responseDTO.setAccessToken(accessToken);
        responseDTO.setRefreshToken(refreshToken);
        responseDTO.setMemberId(member.getMemberId());
        responseDTO.setUserId(member.getUserId());
        responseDTO.setMemberName(member.getMemberName());
        responseDTO.setNickname(member.getNickname());
        responseDTO.setEmail(member.getEmail());
        responseDTO.setUserType(member.getUserType());
        responseDTO.setRole("ROLE_" + member.getUserType());
        responseDTO.setProfileImageUrl(member.getProfileImageUrl());

        log.info("@# REST login 성공 memberId => {}", member.getMemberId());
        log.info("@# REST login 성공 userId => {}", member.getUserId());

        return ResponseEntity.ok(responseDTO);
    }

    // 현재 로그인 사용자 정보 조회
    @GetMapping("/me")
    public ResponseEntity<AuthMeResponseDTO> me(
            @AuthenticationPrincipal EvUserDetails userDetails) {

        log.info("@# /auth/me 요청");

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        log.info("@# /auth/me authentication => {}", authentication);
        log.info("@# /auth/me userDetails => {}", userDetails);

        if (userDetails == null) {
            log.warn("@# /auth/me 실패 userDetails null");
            return ResponseEntity.status(401).build();
        }

        EvMemberDTO member = userDetails.getEvMemberDTO();

        log.info("@# /auth/me memberId => {}", member.getMemberId());
        log.info("@# /auth/me userId => {}", member.getUserId());
        log.info("@# /auth/me userType => {}", member.getUserType());

        AuthMeResponseDTO responseDTO = new AuthMeResponseDTO();
        responseDTO.setMemberId(member.getMemberId());
        responseDTO.setUserId(member.getUserId());
        responseDTO.setMemberName(member.getMemberName());
        responseDTO.setNickname(member.getNickname());
        responseDTO.setEmail(member.getEmail());
        responseDTO.setUserType(member.getUserType());
        responseDTO.setRole("ROLE_" + member.getUserType());
        responseDTO.setProfileImageUrl(member.getProfileImageUrl());
        responseDTO.setLoginType(member.getLoginType());

        return ResponseEntity.ok(responseDTO);
    }

    // AccessToken 재발급
    @PostMapping("/refresh")
    public ResponseEntity<AuthTokenResponseDTO> refresh(
            @Valid @RequestBody AuthRefreshRequestDTO requestDTO) {

        log.info("@# /auth/refresh 요청");

        String refreshToken = requestDTO.getRefreshToken();

        log.info("@# refreshToken => {}", refreshToken);

        if (!jwtTokenProvider.validateToken(refreshToken)) {
            log.warn("@# refresh token invalid");
            return ResponseEntity.status(401).build();
        }

        Long memberId = jwtTokenProvider.getMemberIdFromRefreshToken(refreshToken);
        String savedRefreshToken = jwtRedisTokenService.getRefreshToken(memberId);

        log.info("@# refresh memberId => {}", memberId);
        log.info("@# Redis savedRefreshToken => {}", savedRefreshToken);

        if (savedRefreshToken == null || !savedRefreshToken.equals(refreshToken)) {
            log.warn("@# refresh token mismatch memberId => {}", memberId);
            return ResponseEntity.status(401).build();
        }

        String userId = jwtTokenProvider.getClaims(refreshToken).get("userId", String.class);
        String userType = jwtTokenProvider.getClaims(refreshToken).get("userType", String.class);

        log.info("@# refresh userId => {}", userId);
        log.info("@# refresh userType => {}", userType);

        EvMemberDTO tempMember = new EvMemberDTO();
        tempMember.setMemberId(memberId);
        tempMember.setUserId(userId);
        tempMember.setUserType(userType);
        tempMember.setStatus("ACTIVE");

        EvUserDetails tempUserDetails = new EvUserDetails(tempMember);
        String newAccessToken = jwtTokenProvider.createAccessToken(tempUserDetails);

        log.info("@# newAccessToken => {}", newAccessToken);

        AuthTokenResponseDTO responseDTO = new AuthTokenResponseDTO();
        responseDTO.setAccessToken(newAccessToken);
        responseDTO.setRefreshToken(refreshToken);

        return ResponseEntity.ok(responseDTO);
    }

    // 로그아웃
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
            @RequestBody(required = false) AuthLogoutRequestDTO requestDTO,
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader) {

        log.info("@# /auth/logout 요청");
        log.info("@# logout Authorization Header => {}", authorizationHeader);

        String accessToken = resolveAccessToken(authorizationHeader);

        log.info("@# logout accessToken => {}", accessToken);

        if (accessToken != null && jwtTokenProvider.validateToken(accessToken)) {

            Number memberIdClaim =
                    jwtTokenProvider.getClaims(accessToken).get("memberId", Number.class);

            Long memberId = memberIdClaim.longValue();
            long remainingMs = jwtTokenProvider.getRemainingTimeMs(accessToken);

            log.info("@# logout memberId => {}", memberId);
            log.info("@# logout remainingMs => {}", remainingMs);

            jwtRedisTokenService.addBlackList(accessToken, remainingMs);
            jwtRedisTokenService.deleteRefreshToken(memberId);

            log.info("@# logout 완료 memberId => {}", memberId);
        }

        if (requestDTO != null
                && requestDTO.getRefreshToken() != null
                && jwtTokenProvider.validateToken(requestDTO.getRefreshToken())) {

            Long memberId =
                    jwtTokenProvider.getMemberIdFromRefreshToken(requestDTO.getRefreshToken());

            log.info("@# logout requestDTO refreshToken memberId => {}", memberId);

            jwtRedisTokenService.deleteRefreshToken(memberId);
        }

        SecurityContextHolder.clearContext();

        log.info("@# SecurityContext clear 완료");

        return ResponseEntity.ok().build();
    }

    // Authorization 헤더에서 Bearer 토큰 추출
    private String resolveAccessToken(String authorizationHeader) {
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            return authorizationHeader.substring(7);
        }

        return null;
    }
}