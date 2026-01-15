package org.example.services.Impl;

import lombok.RequiredArgsConstructor;
import org.example.DTOs.request.LoginRequestDTO;
import org.example.DTOs.request.RegisterRequestDTO;
import org.example.DTOs.request.ResendRequestDTO;
import org.example.DTOs.request.VerifyRequestDTO;
import org.example.DTOs.response.AuthResponseDTO;
import org.example.entities.User;
import org.example.entities.VerificationToken;
import org.example.enums.Role;
import org.example.exceptions.*;
import org.example.repositories.UserRepository;
import org.example.repositories.VerificationTokenRepository;
import org.example.security.JwtService;
import org.example.services.MailService;
import org.example.services.UserService;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final MailService mailService;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final VerificationTokenRepository tokenRepository;

    // Qeydiyyat (Register) - TOKENSİZ (Dəyişmir)
    @Override
    @Transactional
    public AuthResponseDTO registerUser(RegisterRequestDTO request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new UserAlreadyExistsException("Bu nömrə və ya email artıq qeydiyyatdan keçib.");
        }

        User user = new User();
        user.setFullName(request.getFullName());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(Role.USER);
        user.setVerified(false);
        User savedUser = userRepository.save(user);

        String otpCode = generateOtp();
        VerificationToken token = new VerificationToken();
        token.setToken(otpCode);
        token.setUser(savedUser);
        token.setExpiryDate(LocalDateTime.now().plusMinutes(5));
        tokenRepository.save(token);

        mailService.sendOtpEmail(user.getEmail(), otpCode);

        // JWT token qaytarılmır.
        return new AuthResponseDTO(true, "Qeydiyyat uğurludur. Email təsdiqi tələb olunur.");
    }

    // Təsdiqləmə (Verify) - İNDİ ARTIQ BURADA DA JWT YARANMIR!
    @Override
    @Transactional
    public AuthResponseDTO verifyUser(VerifyRequestDTO request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new UserNotFoundException("Qeyd olunan email ilə istifadəçi tapılmadı."));

        if (user.isVerified()) {
            throw new UserAlreadyVerifiedException("Bu istifadəçi artıq təsdiqlənib.");
        }

        VerificationToken verificationToken = tokenRepository.findByUser(user)
                .orElseThrow(() -> new InvalidOtpException("Təsdiq kodu yoxdur. Zəhmət olmasa təkrar göndərin."));


        if (verificationToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            tokenRepository.delete(verificationToken);
            throw new OtpExpiredException("OTP kodu etibarlı deyil və ya vaxtı bitib. Zəhmət olmasa yenidən göndərin.");
        }

        if (!request.getOtpCode().equals(verificationToken.getToken())) {
            throw new InvalidOtpException("Daxil etdiyiniz OTP kodu yanlışdır.");
        }

        // Uğurlu təsdiqləmə
        user.setVerified(true);
        userRepository.save(user);
        tokenRepository.delete(verificationToken);

        // *** DƏYİŞİKLİK BURADADIR ***
        // Access və Refresh tokenlər yaradılmır və qaytarılmır.
        return new AuthResponseDTO(true, "Email uğurla təsdiqləndi. İndi giriş edə bilərsiniz.");
    }

    // OTP-ni Yenidən Göndər (Resend) - TOKENSİZ (Dəyişmir)
    @Override
    @Transactional
    public void resendOtp(ResendRequestDTO request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new UserNotFoundException("Qeyd olunan email ilə istifadəçi tapılmadı."));

        if (user.isVerified()) {
            throw new UserAlreadyVerifiedException("Bu istifadəçi artıq təsdiqlənib.");
        }

        tokenRepository.findByUser(user).ifPresent(token -> {
            tokenRepository.delete(token);
            tokenRepository.flush();
        });

        String newOtpCode = generateOtp();
        VerificationToken newToken = new VerificationToken();
        newToken.setToken(newOtpCode);
        newToken.setUser(user);
        newToken.setExpiryDate(LocalDateTime.now().plusMinutes(5));
        tokenRepository.save(newToken);

        mailService.sendOtpEmail(user.getEmail(), newOtpCode);
    }

    // Giriş (Login) - YALNIZ BURADA JWT (ACCESS/REFRESH) YARANIR (Dəyişmir)
    @Override
    @Transactional
    public AuthResponseDTO loginUser(LoginRequestDTO request) {

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new UserNotFoundException("Mobil nömrə və ya şifrə yanlışdır."));

        if (!user.isVerified()) {
            // Təsdiqlənməyibsə: Yeni OTP göndərilir, JWT qaytarılmır.
            tokenRepository.findByUser(user).ifPresent(token -> {
                tokenRepository.delete(token);
                tokenRepository.flush();
            });

            String newOtpCode = generateOtp();
            VerificationToken newToken = new VerificationToken();
            newToken.setToken(newOtpCode);
            newToken.setUser(user);
            newToken.setExpiryDate(LocalDateTime.now().plusMinutes(5));
            tokenRepository.save(newToken);

            mailService.sendOtpEmail(user.getEmail(), newOtpCode);

            throw new UserNotVerifiedException("Giriş üçün email təsdiqi tələb olunur. Yeni OTP kodu email ünvanınıza göndərildi.");
        }

        // UĞURLU GİRİŞ: Access və Refresh tokenlər yaradılır.
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            user.getEmail(),
                            request.getPassword()
                    )
            );

            String accessToken = jwtService.generateToken(user);
            String refreshToken = jwtService.generateRefreshToken(user);

            return new AuthResponseDTO(true, "Giriş uğurludur.", accessToken, refreshToken);

        } catch (AuthenticationException e) {
            throw new InvalidCredentialsException("Mobil nömrə və ya şifrə yanlışdır.");
        }
    }

    // REFRESH TOKEN METODU (Dəyişmir)
    @Override
    public AuthResponseDTO refreshToken(String refreshToken) {
        Object tokenType = null;
        try {
            tokenType = jwtService.exportToken(refreshToken, claims -> claims.get("type"));
        } catch (Exception e) {
            throw new InvalidTokenException("Daxil edilən token etibarlı deyil və ya vaxtı bitib.");
        }

        if (!"REFRESH".equals(tokenType)) {
            throw new InvalidTokenException("Daxil edilən token növü Refresh Token deyil.");
        }

        String userEmail = jwtService.findUsername(refreshToken);

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new UserNotFoundException("Refresh Token-ə uyğun istifadəçi tapılmadı."));

        if (jwtService.isTokenExpired(refreshToken)) {
            throw new InvalidTokenException("Refresh Token-in vaxtı bitib. Zəhmət olmasa yenidən giriş edin.");
        }

        String newAccessToken = jwtService.generateToken(user);
        String newRefreshToken = jwtService.generateRefreshToken(user);

        return new AuthResponseDTO(true, "Tokenlər uğurla yeniləndi.", newAccessToken, newRefreshToken);
    }


    private String generateOtp() {
        Random random = new Random();
        return String.format("%04d", random.nextInt(10000));
    }
}