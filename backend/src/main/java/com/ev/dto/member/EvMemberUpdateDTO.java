package com.ev.dto.member;

import lombok.Data;

/*
 * 회원정보 수정 DTO
 * current/new password는 화면 입력값이고,
 * password는 암호화 후 DB update용 값이다.
 */
@Data
public class EvMemberUpdateDTO {

    private Long memberId;

    private String nickname;

    private String currentPassword;
    private String newPassword;
    private String newPasswordConfirm;

    private String password;

    private String profileImageUrl;
}