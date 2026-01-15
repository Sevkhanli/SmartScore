package org.example.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.DTOs.request.LoginRequestDTO;
import org.example.DTOs.request.RegisterRequestDTO;
import org.example.DTOs.request.ResendRequestDTO;
import org.example.DTOs.request.VerifyRequestDTO;
import org.example.DTOs.response.AuthResponseDTO;
import org.example.exceptions.InvalidTokenException;
import org.example.services.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

    @PostMapping("/register")
    public ResponseEntity<AuthResponseDTO> register(@Valid @RequestBody RegisterRequestDTO request) {
        // Register token qaytarmır.
        return ResponseEntity.status(HttpStatus.CREATED).body(userService.registerUser(request));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponseDTO> login(@Valid @RequestBody LoginRequestDTO request) {
        return ResponseEntity.ok(userService.loginUser(request));
    }

    // Tokensiz təsdiqləmə. Body-dən DTO (Email + OTP) qəbul edir.
    @PostMapping("/verify")
    public ResponseEntity<AuthResponseDTO> verify(@Valid @RequestBody VerifyRequestDTO request) {
        return ResponseEntity.ok(userService.verifyUser(request));
    }

    // Tokensiz təkrar göndərmə. Body-dən DTO (Email) qəbul edir.
    @PostMapping("/resend-otp")
    public ResponseEntity<AuthResponseDTO> resendOtp(@Valid @RequestBody ResendRequestDTO request) {

        userService.resendOtp(request);

        return ResponseEntity.ok(new AuthResponseDTO(true, "Yeni OTP kodu email ünvanınıza göndərildi."));
    }

    // Refresh Token endpoint-i. Token header-dən qəbul olunur.
    @PostMapping("/refresh-token")
    public ResponseEntity<AuthResponseDTO> refreshToken(
            @RequestHeader("Authorization") String authHeader) {

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new InvalidTokenException("Refresh Token 'Authorization' header-də tapılmadı və ya düzgün formatda deyil.");
        }

        String refreshToken = authHeader.substring(7);

        AuthResponseDTO response = userService.refreshToken(refreshToken);

        return ResponseEntity.ok(response);
    }
    @PostMapping("/logout")
    public ResponseEntity<AuthResponseDTO> logout() {
        // Cari sessiya məlumatlarını (əgər varsa) təmizləyir
        org.springframework.security.core.context.SecurityContextHolder.clearContext();

        return ResponseEntity.ok(new AuthResponseDTO(true, "Çıxış uğurla tamamlandı."));
    }
}