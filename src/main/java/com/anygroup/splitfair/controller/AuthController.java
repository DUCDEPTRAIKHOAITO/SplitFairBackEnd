package com.anygroup.splitfair.controller;

import com.anygroup.splitfair.dto.APIResponse;
import com.anygroup.splitfair.dto.Auth.*;
import com.anygroup.splitfair.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<APIResponse<AuthResponse>> register(@RequestBody RegisterRequest request) {
        try {
            AuthResponse authResponse = authService.register(request);
            APIResponse<AuthResponse> response = new APIResponse<>(
                    "success",
                    "Registration successful",
                    authResponse,
                    null,
                    LocalDateTime.now()
            );
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            APIResponse<AuthResponse> response = new APIResponse<>(
                    "error",
                    e.getMessage(),
                    null,
                    e.getMessage(),
                    LocalDateTime.now()
            );
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping("/login")
    public ResponseEntity<APIResponse<AuthResponse>> login(@RequestBody LoginRequest request) {
        try {
            AuthResponse authResponse = authService.login(request);
            APIResponse<AuthResponse> response = new APIResponse<>(
                    "success",
                    "Login successful",
                    authResponse,
                    null,
                    LocalDateTime.now()
            );
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            APIResponse<AuthResponse> response = new APIResponse<>(
                    "error",
                    e.getMessage(),
                    null,
                    e.getMessage(),
                    LocalDateTime.now()
            );
            return ResponseEntity.badRequest().body(response);
        }
    }
}
