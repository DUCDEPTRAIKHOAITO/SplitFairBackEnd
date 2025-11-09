package com.anygroup.splitfair.service;

import com.anygroup.splitfair.dto.Auth.AuthResponse;
import com.anygroup.splitfair.dto.Auth.LoginRequest;
import com.anygroup.splitfair.dto.Auth.RegisterRequest;

public interface AuthService {
    AuthResponse register(RegisterRequest request);
    AuthResponse login(LoginRequest request);
}
