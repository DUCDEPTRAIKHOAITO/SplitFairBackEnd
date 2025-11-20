package com.anygroup.splitfair.dto.Auth;

import lombok.Data;

import java.util.UUID;

@Data
public class AuthResponse {
    private String token;
    private String userName;
    private String role;
    private UUID userId;  // Trả về userId
    private String email;
}