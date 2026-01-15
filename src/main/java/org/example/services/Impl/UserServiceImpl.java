package org.example.services.Impl;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import lombok.RequiredArgsConstructor;
import org.example.DTOs.request.*;
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
import org.springframework.beans.factory.annotation.Value; // Lombok deyil, Spring Value olmalıdır
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Random;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final MailService mailService;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final VerificationTokenRepository tokenRepository;


    @Value("${google.id}")
    private String googleClientId;

    @Override
    @Transactional
    public AuthResponseDTO registerUser(RegisterRequestDTO request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new UserAlreadyExistsException("Bu email artıq qeydiyyatdan keçib.");
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

        return new AuthResponseDTO(true, "Qeydiyyat uğurludur. Email təsdiqi tələb olunur.");
    }

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

        user.setVerified(true);
        userRepository.save(user);
        tokenRepository.delete(verificationToken);

        return new AuthResponseDTO(true, "Email uğurla təsdiqləndi. İndi giriş edə bilərsiniz.");
    }

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

    @Override
    @Transactional
    public AuthResponseDTO loginUser(LoginRequestDTO request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new UserNotFoundException("Email və ya şifrə yanlışdır."));

        if (!user.isVerified()) {
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

        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(user.getEmail(), request.getPassword())
            );

            String accessToken = jwtService.generateToken(user);
            String refreshToken = jwtService.generateRefreshToken(user);

            return new AuthResponseDTO(true, "Giriş uğurludur.", accessToken, refreshToken);
        } catch (AuthenticationException e) {
            throw new InvalidCredentialsException("Email və ya şifrə yanlışdır.");
        }
    }

    @Override
    @Transactional
    public AuthResponseDTO loginWithGoogle(GoogleLoginRequestDTO request) {
        try {
            // Google verifier qurulur
            GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(new NetHttpTransport(), new GsonFactory())
                    .setAudience(Collections.singletonList(googleClientId))
                    .build();

            // Token yoxlanılır
            GoogleIdToken idToken = verifier.verify(request.getIdToken());
            if (idToken == null) {
                throw new InvalidTokenException("Google tokeni etibarsızdır.");
            }

            GoogleIdToken.Payload payload = idToken.getPayload();
            String email = payload.getEmail();
            String name = (String) payload.get("name");

            // İstifadəçi bazada yoxdursa qeydiyyat edilir
            User user = userRepository.findByEmail(email).orElseGet(() -> {
                User newUser = new User();
                newUser.setEmail(email);
                newUser.setFullName(name);
                // Google ilə gələnlər üçün təsadüfi şifrə təyin edilir
                newUser.setPassword(passwordEncoder.encode(UUID.randomUUID().toString()));
                newUser.setRole(Role.USER);
                newUser.setVerified(true);
                return userRepository.save(newUser);
            });

            // Sistemin öz JWT tokenləri yaradılır
            String accessToken = jwtService.generateToken(user);
            String refreshToken = jwtService.generateRefreshToken(user);

            return new AuthResponseDTO(true, "Google ilə giriş uğurludur.", accessToken, refreshToken);
        } catch (Exception e) {
            throw new RuntimeException("Google autentifikasiya xətası: " + e.getMessage());
        }
    }
    @Override
    public AuthResponseDTO refreshToken(String refreshToken) {
        String userEmail = jwtService.findUsername(refreshToken);
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new UserNotFoundException("İstifadəçi tapılmadı."));

        if (jwtService.isTokenExpired(refreshToken)) {
            throw new InvalidTokenException("Refresh Token-in vaxtı bitib.");
        }

        String newAccessToken = jwtService.generateToken(user);
        String newRefreshToken = jwtService.generateRefreshToken(user);

        return new AuthResponseDTO(true, "Tokenlər yeniləndi.", newAccessToken, newRefreshToken);
    }

    private String generateOtp() {
        Random random = new Random();
        return String.format("%04d", random.nextInt(10000));
    }
}