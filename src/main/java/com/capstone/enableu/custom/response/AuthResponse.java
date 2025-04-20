package com.capstone.enableu.custom.response;

import lombok.Data;

@Data
public class AuthResponse {
    private String accessToken;
    private String refreshToken;
}
