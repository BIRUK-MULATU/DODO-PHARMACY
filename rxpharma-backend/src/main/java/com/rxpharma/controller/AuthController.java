package com.rxpharma.controller;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.rxpharma.dto.request.ForgotPasswordRequest;
import com.rxpharma.dto.request.GoogleTokenRequest;
import com.rxpharma.dto.request.LoginRequest;
import com.rxpharma.dto.request.RegisterRequest;
import com.rxpharma.dto.request.ResetPasswordRequest;
import com.rxpharma.dto.response.AuthResponse;
import com.rxpharma.entity.User;
import com.rxpharma.repository.UserRepository;
import com.rxpharma.security.JwtUtil;
import com.rxpharma.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;

    @Value("${google.client.id}")
    private String googleClientId;

    @PostMapping("/login")
    public ResponseEntity<?> login(
            @Valid @RequestBody LoginRequest request) {
        User user = authService.login(request.getEmail(), request.getPassword());
        if (!user.isApproved()) {
            return ResponseEntity.status(403).body(Map.of(
                    "message", "Your account is pending admin approval."
            ));
        }
        String token = jwtUtil.generateToken(user.getEmail(), user.getRole().name());
        return ResponseEntity.ok(new AuthResponse(
                token, user.getId(), user.getEmail(),
                user.getFullName(), user.getRole().name()
        ));
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(
            @Valid @RequestBody RegisterRequest request) {
        String token = authService.register(
                request.getFullName(), request.getEmail(),
                request.getPassword(), request.getRole()
        );
        User user = userRepository.findByEmail(request.getEmail()).orElseThrow();
        return ResponseEntity.ok(new AuthResponse(
                token, user.getId(), user.getEmail(),
                user.getFullName(), user.getRole().name()
        ));
    }

    // NEW: Google OAuth login
    @PostMapping("/google")
    public ResponseEntity<?> googleLogin(@RequestBody GoogleTokenRequest request) {
        try {
            GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(
                    new NetHttpTransport(), new GsonFactory())
                    .setAudience(Collections.singletonList(googleClientId))
                    .build();

            GoogleIdToken idToken = verifier.verify(request.getCredential());
            if (idToken == null) {
                return ResponseEntity.badRequest()
                        .body(Map.of("message", "Invalid Google token"));
            }

            GoogleIdToken.Payload payload = idToken.getPayload();
            String email = payload.getEmail();
            String fullName = (String) payload.get("name");

            // Check if user already exists
            User existing = userRepository.findByEmail(email).orElse(null);

            if (existing == null) {
                // New Google sign-in — create as PENDING, NOT approved
                User newUser = User.builder()
                        .email(email)
                        .fullName(fullName != null ? fullName : email)
                        .password(passwordEncoder.encode(java.util.UUID.randomUUID().toString()))
                        .role(User.Role.CASHIER) // default lowest-privilege role
                        .approved(false)
                        .authProvider("GOOGLE")
                        .build();
                userRepository.save(newUser);
                return ResponseEntity.status(403).body(Map.of(
                        "message", "Your account has been created but is pending admin approval. Please contact an administrator."
                ));
            }

            if (!existing.isApproved()) {
                return ResponseEntity.status(403).body(Map.of(
                        "message", "Your account is pending admin approval. Please contact an administrator."
                ));
            }

            String token = jwtUtil.generateToken(existing.getEmail(), existing.getRole().name());
            return ResponseEntity.ok(new AuthResponse(
                    token, existing.getId(), existing.getEmail(),
                    existing.getFullName(), existing.getRole().name()
            ));

        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "Google authentication failed: " + e.getMessage()));
        }
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
                request.getToken(), request.getNewPassword(), request.getConfirmPassword()
        );
        return ResponseEntity.ok(Map.of("message", "Password reset successfully"));
    }
}