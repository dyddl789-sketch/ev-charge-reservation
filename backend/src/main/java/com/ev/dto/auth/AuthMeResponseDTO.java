package com.ev.dto.auth;

import lombok.Data;

@Data
public class AuthMeResponseDTO {

    private Long memberId;
    private String userId;
    private String memberName;
    private String nickname;
    private String email;
    private String userType;
    private String role;
    private String profileImageUrl;
    private String loginType;
}
