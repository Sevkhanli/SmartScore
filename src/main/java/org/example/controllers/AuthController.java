package org.example.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.DTOs.request.*;
import org.example.DTOs.response.AuthResponseDTO;
import org.example.exceptions.InvalidTokenException;
import org.example.services.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

    @PostMapping("/register")
    public ResponseEntity<AuthResponseDTO> register(@Valid @RequestBody RegisterRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(userService.registerUser(request));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponseDTO> login(@Valid @RequestBody LoginRequestDTO request) {
        return ResponseEntity.ok(userService.loginUser(request));
    }

    @PostMapping("/verify")
    public ResponseEntity<AuthResponseDTO> verify(@Valid @RequestBody VerifyRequestDTO request) {
        return ResponseEntity.ok(userService.verifyUser(request));
    }

    @PostMapping("/resend-otp")
    public ResponseEntity<AuthResponseDTO> resendOtp(@Valid @RequestBody ResendRequestDTO request) {
        userService.resendOtp(request);
        return ResponseEntity.ok(new AuthResponseDTO(true, "Yeni OTP kodu email ünvanınıza göndərildi."));
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<AuthResponseDTO> refreshToken(@RequestHeader("Authorization") String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new InvalidTokenException("Refresh Token düzgün formatda deyil.");
        }
        String refreshToken = authHeader.substring(7);
        return ResponseEntity.ok(userService.refreshToken(refreshToken));
    }

    @PostMapping("/google")
    public ResponseEntity<AuthResponseDTO> googleLogin(@Valid @RequestBody GoogleLoginRequestDTO request) {
        return ResponseEntity.ok(userService.loginWithGoogle(request));
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<AuthResponseDTO> forgotPassword(@Valid @RequestBody ForgotPasswordRequestDTO request) {
        userService.forgotPassword(request);
        return ResponseEntity.ok(new AuthResponseDTO(true, "Şifrə sıfırlama kodu emailinizə göndərildi."));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<AuthResponseDTO> resetPassword(@Valid @RequestBody ResetPasswordRequestDTO request) {
        return ResponseEntity.ok(userService.resetPassword(request));
    }

    @PostMapping("/resend-forgot-password-otp")
    public ResponseEntity<AuthResponseDTO> resendForgotPasswordOtp(@Valid @RequestBody ResendRequestDTO request) {
        userService.resendForgotPasswordOtp(request);
        return ResponseEntity.ok(new AuthResponseDTO(true, "Şifrə sıfırlama kodu yenidən email ünvanınıza göndərildi."));
    }

    @PostMapping("/logout")
    public ResponseEntity<String> logout(
            @RequestHeader("Authorization") String authHeader,
            // required = true edirik ki, göndərilməyəndə 400 xətası versin
            @RequestHeader(value = "X-Refresh-Token", required = true) String refreshToken) {

        userService.logout(authHeader, refreshToken);
        return ResponseEntity.ok("Uğurla çıxış edildi.");
    }

    @GetMapping("/me")
    public ResponseEntity<AuthResponseDTO> getMyProfile() {
        // Token-dən gələn email məlumatını götürürük
        org.springframework.security.core.Authentication auth =
                SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || !auth.isAuthenticated() || auth.getName().equals("anonymousUser")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new AuthResponseDTO(false, "Sessiya tapılmadı. Zəhmət olmasa giriş edin."));
        }

        String email = auth.getName();
        // Servisdən gələn cavabı birbaşa qaytarırıq
        return ResponseEntity.ok(userService.getUserProfile(email));
    }
    }
