package com.ev.dto.auth;

import lombok.Data;

@Data
public class AuthLogoutRequestDTO {

    private String refreshToken;
}
