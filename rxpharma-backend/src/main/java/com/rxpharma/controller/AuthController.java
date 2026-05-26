package com.rxpharma.controller;

import com.rxpharma.dto.request.ForgotPasswordRequest;
import com.rxpharma.dto.request.LoginRequest;
import com.rxpharma.dto.request.RegisterRequest;
import com.rxpharma.dto.request.ResetPasswordRequest;
import com.rxpharma.dto.response.AuthResponse;
import com.rxpharma.entity.User;
import com.rxpharma.repository.UserRepository;
import com.rxpharma.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final UserRepository userRepository;

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(
            @Valid @RequestBody LoginRequest request) {
        String token = authService.login(request.getEmail(), request.getPassword());
        User user = userRepository.findByEmail(request.getEmail()).orElseThrow();
        return ResponseEntity.ok(new AuthResponse(token, user.getEmail(), user.getRole().name()));
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(
            @Valid @RequestBody RegisterRequest request) {
        String token = authService.register(
                request.getFullName(), request.getEmail(),
                request.getPassword(), request.getRole()
        );
        return ResponseEntity.ok(new AuthResponse(
                token, request.getEmail(), request.getRole().name()));
    }

    @PostMapping("/logout")
    public ResponseEntity<Map<String, String>> logout(
            @RequestHeader("Authorization") String authHeader) {
        return ResponseEntity.ok(Map.of("message", "Logged out successfully"));
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<Map<String, String>> forgotPassword(
            @Valid @RequestBody ForgotPasswordRequest request) {
        String token = authService.forgotPassword(request.getEmail());
        return ResponseEntity.ok(Map.of(
                "message", "Password reset token generated successfully",
                "resetToken", token,
                "expiresIn", "30 minutes",
                "note", "In production this token would be sent to your email"
        ));
    }

    @PostMapping("/verify-reset-token")
    public ResponseEntity<Map<String, Object>> verifyResetToken(
            @RequestBody Map<String, String> body) {
        boolean valid = authService.verifyResetToken(body.get("token"));
        return ResponseEntity.ok(Map.of(
                "valid", valid,
                "message", valid ? "Token is valid" : "Token is invalid or expired"
        ));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<Map<String, String>> resetPassword(
            @Valid @RequestBody ResetPasswordRequest request) {
        authService.resetPassword(
                request.getToken(),
                request.getNewPassword(),
                request.getConfirmPassword()
        );
        return ResponseEntity.ok(Map.of("message", "Password reset successfully"));
    }
}