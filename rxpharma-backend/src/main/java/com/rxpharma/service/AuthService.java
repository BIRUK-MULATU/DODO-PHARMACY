package com.rxpharma.service;

import com.rxpharma.entity.PasswordResetToken;
import com.rxpharma.entity.User;
import com.rxpharma.repository.PasswordResetTokenRepository;
import com.rxpharma.repository.UserRepository;
import com.rxpharma.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;
    private final PasswordResetTokenRepository tokenRepository;

    public String login(String email, String password) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(email, password)
        );
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return jwtUtil.generateToken(user.getEmail(), user.getRole().name());
    }

    public String register(String fullName, String email,
                           String password, User.Role role) {
        if (userRepository.existsByEmail(email)) {
            throw new RuntimeException("Email already in use");
        }
        User user = User.builder()
                .fullName(fullName)
                .email(email)
                .password(passwordEncoder.encode(password))
                .role(role)
                .build();
        userRepository.save(user);
        return jwtUtil.generateToken(user.getEmail(), user.getRole().name());
    }

    @Transactional
    public String forgotPassword(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new com.rxpharma.exception.ResourceNotFoundException(
                        "No account found with email: " + email));

        // Delete any existing tokens for this user
        tokenRepository.deleteByUserId(user.getId());

        // Generate a new token
        String token = UUID.randomUUID().toString().replace("-", "").substring(0, 32).toUpperCase();

        PasswordResetToken resetToken = PasswordResetToken.builder()
                .user(user)
                .token(token)
                .expiresAt(LocalDateTime.now().plusMinutes(30))
                .used(false)
                .build();

        tokenRepository.save(resetToken);

        // In production this token would be emailed to the user
        // For now we return it directly in the response
        return token;
    }

    @Transactional
    public void resetPassword(String token, String newPassword, String confirmPassword) {
        if (!newPassword.equals(confirmPassword)) {
            throw new com.rxpharma.exception.BadRequestException(
                    "New password and confirm password do not match");
        }

        PasswordResetToken resetToken = tokenRepository.findByToken(token)
                .orElseThrow(() -> new com.rxpharma.exception.BadRequestException(
                        "Invalid reset token"));

        if (resetToken.isExpired()) {
            throw new com.rxpharma.exception.BadRequestException(
                    "Reset token has expired. Please request a new one.");
        }

        if (resetToken.isUsed()) {
            throw new com.rxpharma.exception.BadRequestException(
                    "Reset token has already been used.");
        }

        User user = resetToken.getUser();
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        resetToken.setUsed(true);
        tokenRepository.save(resetToken);
    }

    public boolean verifyResetToken(String token) {
        return tokenRepository.findByToken(token)
                .map(t -> !t.isExpired() && !t.isUsed())
                .orElse(false);
    }
}