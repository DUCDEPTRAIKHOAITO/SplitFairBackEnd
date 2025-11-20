package com.anygroup.splitfair.service.impl;

import com.anygroup.splitfair.dto.Auth.AuthResponse;
import com.anygroup.splitfair.dto.Auth.LoginRequest;
import com.anygroup.splitfair.dto.Auth.RegisterRequest;
import com.anygroup.splitfair.enums.RoleType;
import com.anygroup.splitfair.model.Role;
import com.anygroup.splitfair.model.User;
import com.anygroup.splitfair.repository.RoleRepository;
import com.anygroup.splitfair.repository.UserRepository;
import com.anygroup.splitfair.service.AuthService;
import com.anygroup.splitfair.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @Override
    public AuthResponse register(RegisterRequest request) {
        // Kiểm tra tính hợp lệ của email
        if (!isValidEmail(request.getEmail())) {
            throw new RuntimeException("Invalid email format");
        }

        // Kiểm tra email đã tồn tại chưa
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new RuntimeException("Email already registered");
        }

        // Lấy Role USER từ cơ sở dữ liệu
        Role role = roleRepository.findByName(RoleType.USER)
                .orElseThrow(() -> new RuntimeException("Role USER not found"));

        // Tạo người dùng mới
        User user = User.builder()
                .userName(request.getUserName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(role)
                .build();

        userRepository.save(user);

        // Tạo token JWT cho người dùng
        String token = jwtUtil.generateToken(user.getEmail());

        // Trả về thông tin người dùng và token
        AuthResponse res = new AuthResponse();
        res.setToken(token);
        res.setUserName(user.getUserName());
        res.setRole(user.getRole().getName().name());
        res.setUserId(user.getId()); // Trả về userId
        res.setEmail(user.getEmail()); // Trả về email
        return res;
    }

    @Override
    public AuthResponse login(LoginRequest request) {
        // Kiểm tra tính hợp lệ của email
        if (!isValidEmail(request.getEmail())) {
            throw new RuntimeException("Invalid email format");
        }

        // Tìm người dùng theo email
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Kiểm tra mật khẩu
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("Invalid email or password");
        }

        // Tạo token JWT
        String token = jwtUtil.generateToken(user.getEmail());

        // Trả về thông tin người dùng và token
        AuthResponse res = new AuthResponse();
        res.setToken(token);
        res.setUserName(user.getUserName());
        res.setRole(user.getRole().getName().name());
        res.setUserId(user.getId()); // Trả về userId
        res.setEmail(user.getEmail()); // Trả về email
        return res;
    }

    // Phương thức kiểm tra tính hợp lệ của email
    private boolean isValidEmail(String email) {
        String regex = "^[A-Za-z0-9+_.-]+@(.+)$";  // Biểu thức chính quy kiểm tra email hợp lệ
        return email.matches(regex);
    }
}
