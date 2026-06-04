package com.ev.service.user;

import org.springframework.web.multipart.MultipartFile;

import com.ev.dto.member.EvMemberDTO;
import com.ev.dto.member.EvMemberUpdateDTO;

public interface EvMemberService {

    // 회원가입 처리
    void join(EvMemberDTO evMemberDTO, MultipartFile profileImage) throws Exception;

    // 회원번호로 회원 조회
    EvMemberDTO findByMemberId(Long memberId);

    // 회원정보 수정
    void updateMember(EvMemberUpdateDTO updateDTO, MultipartFile profileImage) throws Exception;

    // 아이디 중복검사
    boolean isUserIdAvailable(String userId);

    // 이메일 인증번호 발송
    void sendEmailCode(String email);

    // 이메일 인증번호 확인
    boolean verifyEmailCode(String email, String code);
    
 // 닉네임 중복확인
    boolean isNicknameAvailable(String nickname);

    // 이메일 중복확인
    boolean isEmailAvailable(String email);

    // 휴대폰 번호 중복확인
    boolean isPhoneAvailable(String phone);
}