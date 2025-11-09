package com.anygroup.splitfair.config;

import com.anygroup.splitfair.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        // Cho phép login/register không cần xác thực
                        .requestMatchers("/api/auth/**").permitAll()

                        // ✅ Cho phép ADMIN hoặc LEADER xóa expense
                        .requestMatchers(org.springframework.http.HttpMethod.DELETE, "/api/expenses/**")
                        .hasAnyAuthority("ADMIN", "LEADER")

                        // ✅ Các API khác yêu cầu đăng nhập
                        .requestMatchers("/api/expenses/**",
                                "/api/bills/**",
                                "/api/groups/**",
                                "/api/categories/**",
                                "/api/users/**",
                                "/api/attachments/**",
                                "/api/expense-shares/**",
                                "/uploads/**").authenticated()

                        .anyRequest().authenticated()
                )
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
