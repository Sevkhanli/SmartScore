package az.edu.itbrains.SmartScore.services;

import az.edu.itbrains.SmartScore.dtos.request.*;
import az.edu.itbrains.SmartScore.dtos.response.AuthResponseDTO;
import az.edu.itbrains.SmartScore.models.User;

public interface UserService {
    User findByTelegramChatId(Long chatId);
    boolean linkTelegram(String email, Long chatId);

    User getCurrentUser();

    AuthResponseDTO registerUser(RegisterRequestDTO request);

    // Tokensiz təsdiqləmə.
    AuthResponseDTO verifyUser(VerifyRequestDTO request);

    // Tokensiz təkrar göndərmə.
    void resendOtp(ResendRequestDTO request);

    AuthResponseDTO loginUser(LoginRequestDTO request);

    AuthResponseDTO refreshToken(String refreshToken);

    AuthResponseDTO loginWithGoogle(GoogleLoginRequestDTO request);

    AuthResponseDTO getUserProfile(String email);

    void forgotPassword(ForgotPasswordRequestDTO request);
    AuthResponseDTO resetPassword(ResetPasswordRequestDTO request);

    void logout(String authHeader, String refreshToken);

    void resendForgotPasswordOtp(ResendRequestDTO request); // Yeni əlavə edildi
}
