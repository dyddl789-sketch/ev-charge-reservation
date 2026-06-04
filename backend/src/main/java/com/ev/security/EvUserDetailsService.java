package com.ev.security;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.ev.dao.user.EvMemberDAO;
import com.ev.dto.member.EvMemberDTO;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Spring Security 로그인 사용자 조회 서비스
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EvUserDetailsService implements UserDetailsService {

    private final EvMemberDAO evMemberDAO;

    /**
     * 로그인 요청 시 Spring Security가 자동으로 호출
     */
    @Override
    public UserDetails loadUserByUsername(String userId)
            throws UsernameNotFoundException {

        log.info("@# EvUserDetailsService.loadUserByUsername()");
        log.info("@# userId => {}", userId);

        // DB에서 userId로 회원 조회
        EvMemberDTO evMemberDTO = evMemberDAO.findByUserId(userId);

        // 회원이 없으면 로그인 실패 처리
        if (evMemberDTO == null) {
            throw new UsernameNotFoundException("회원을 찾을 수 없습니다.");
        }

        // Security가 사용할 사용자 객체 반환
        return new EvUserDetails(evMemberDTO);
    }
}