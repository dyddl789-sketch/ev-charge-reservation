package com.ev.dto.auth;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AuthRefreshRequestDTO {

    @NotBlank(message = "Refresh Token이 없습니다.")
    private String refreshToken;
}
