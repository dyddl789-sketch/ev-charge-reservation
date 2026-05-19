package com.ev.dto;

import lombok.Data;

@Data
public class MemberDTO {

    private int memberId;
    private String email;
    private String password;
    private String nickname;
}