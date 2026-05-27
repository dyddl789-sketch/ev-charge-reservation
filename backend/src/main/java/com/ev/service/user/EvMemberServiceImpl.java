package com.ev.service.user;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ev.dao.user.EvMemberDAO;
import com.ev.dto.member.EvMemberDTO;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class EvMemberServiceImpl implements EvMemberService {

    private final EvMemberDAO evMemberDAO;
    private final PasswordEncoder passwordEncoder;

    /*
     * 회원가입 처리
     *
     * 처리 순서:
     * 1. 아이디 중복 확인
     * 2. 이메일 중복 확인
     * 3. 전화번호 중복 확인
     * 4. 비밀번호 BCrypt 암호화
     * 5. 기본값 설정
     * 6. 회원 정보 DB 저장
     */
    @Override
    @Transactional
    public void join(EvMemberDTO evMemberDTO) {
        log.info("@# EvMemberServiceImpl.join()");
        log.info("@# evMemberDTO => {}", evMemberDTO);

        // 아이디 중복 확인
        if (evMemberDAO.countByUserId(evMemberDTO.getUserId()) > 0) {
            throw new IllegalArgumentException("이미 사용 중인 아이디입니다.");
        }

        // 이메일 중복 확인
        if (evMemberDAO.countByEmail(evMemberDTO.getEmail()) > 0) {
            throw new IllegalArgumentException("이미 사용 중인 이메일입니다.");
        }

        // 전화번호 중복 확인
        if (evMemberDTO.getPhone() != null
                && !evMemberDTO.getPhone().trim().isEmpty()) {

            if (evMemberDAO.countByPhone(evMemberDTO.getPhone()) > 0) {
                throw new IllegalArgumentException("이미 사용 중인 전화번호입니다.");
            }
        }

        // 비밀번호 BCrypt 암호화
        evMemberDTO.setPassword(
                passwordEncoder.encode(evMemberDTO.getPassword())
        );

        // 기본값 설정
        evMemberDTO.setUserType("USER");
        evMemberDTO.setLoginType("LOCAL");
        evMemberDTO.setStatus("ACTIVE");

        // 회원 정보 저장
        evMemberDAO.insertMember(evMemberDTO);

        log.info("@# 회원가입 완료 userId => {}", evMemberDTO.getUserId());
    }
}