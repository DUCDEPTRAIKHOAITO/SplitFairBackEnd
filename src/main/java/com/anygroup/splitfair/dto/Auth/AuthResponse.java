package com.anygroup.splitfair.dto.Auth;

import lombok.Data;

@Data
public class AuthResponse {
    private String token;
    private String userName;
    private String role;
}