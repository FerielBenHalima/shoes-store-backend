package com.shoes_store.backend.services;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.shoes_store.backend.dto.request.LoginRequest;
import com.shoes_store.backend.dto.response.AuthResponse;
import com.shoes_store.backend.models.Admin;
import com.shoes_store.backend.repositories.AdminRepository;
import com.shoes_store.backend.security.JwtService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final AdminRepository adminRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthResponse login(LoginRequest request) {
        Admin admin = adminRepository.findByEmail(request.getEmail())
            .orElseThrow(() -> new RuntimeException("Invalid credentials"));

        if (!passwordEncoder.matches(request.getPassword(), admin.getPassword())) {
            throw new RuntimeException("Invalid credentials");
        }

        String token = jwtService.generateToken(admin.getEmail());
        return new AuthResponse(token, admin.getEmail());
    }
}
