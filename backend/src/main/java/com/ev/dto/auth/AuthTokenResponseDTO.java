package com.ev.dto.auth;

import lombok.Data;

@Data
public class AuthTokenResponseDTO {

    private String accessToken;
    private String refreshToken;
    private String tokenType = "Bearer";
}
