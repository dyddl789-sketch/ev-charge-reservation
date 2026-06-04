package com.ev.dto.member;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class EvMemberDTO {

    // 회원 고유 번호
    private Long memberId;

    // 로그인 아이디
    private String userId;

    // 비밀번호
    // LOCAL 회원은 값이 있고, 소셜 로그인 회원은 null 가능
    private String password;

    // 회원 이름
    private String memberName;

    // 닉네임
    private String nickname;

    // 전화번호
    private String phone;

    // 이메일
    private String email;

    // 프로필 이미지 URL
    private String profileImageUrl;

    // 회원 유형: USER / ADMIN
    private String userType;

    // 로그인 유형: LOCAL / GOOGLE / KAKAO / NAVER
    private String loginType;

    // 회원 상태: ACTIVE / INACTIVE / BLOCKED
    private String status;

    // 마지막 로그인 시간
    private LocalDateTime lastLoginAt;

    // 가입일
    private LocalDateTime createdAt;

    // 수정일
    private LocalDateTime updatedAt;
}
