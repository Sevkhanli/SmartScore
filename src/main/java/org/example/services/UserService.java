package org.example.services;

import org.example.DTOs.request.*;
import org.example.DTOs.response.AuthResponseDTO;

public interface UserService {
    AuthResponseDTO registerUser(RegisterRequestDTO request);

    // Tokensiz təsdiqləmə.
    AuthResponseDTO verifyUser(VerifyRequestDTO request);

    // Tokensiz təkrar göndərmə.
    void resendOtp(ResendRequestDTO request);

    AuthResponseDTO loginUser(LoginRequestDTO request);

    AuthResponseDTO refreshToken(String refreshToken);

    AuthResponseDTO loginWithGoogle(GoogleLoginRequestDTO request);

    AuthResponseDTO getUserProfile(String email);




}