package org.example.services.Impl;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import lombok.RequiredArgsConstructor;
import org.example.DTOs.request.*;
import org.example.DTOs.response.AuthResponseDTO;
import org.example.entities.RevokedToken;
import org.example.entities.User;
import org.example.entities.VerificationToken;
import org.example.enums.Role;
import org.example.exceptions.*;
import org.example.repositories.RevokedTokenRepository;
import org.example.repositories.UserRepository;
import org.example.repositories.VerificationTokenRepository;
import org.example.security.JwtService;
import org.example.services.MailService;
import org.example.services.UserService;
import org.springframework.beans.factory.annotation.Value;
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
    private final RevokedTokenRepository revokedTokenRepository;

    @Value("${google.id}")
    private String googleClientId;

    @Override
    @Transactional
    public AuthResponseDTO registerUser(RegisterRequestDTO request) {
        // 1. Şifrələrin uyğunluğunu yoxla
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new InvalidCredentialsException("Şifrələr bir-biri ilə uyğun gəlmir.");
        }

        // 2. Email yoxlanışı
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
        // Qeydiyyat olduğu üçün isResetPassword = false
        sendOrUpdateOtp(savedUser, false);

        return new AuthResponseDTO(true, "Qeydiyyat uğurludur. Email təsdiqi tələb olunur.");
    }

    @Override
    @Transactional
    public void resendOtp(ResendRequestDTO request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new UserNotFoundException("İstifadəçi tapılmadı."));
        if (user.isVerified()) throw new UserAlreadyVerifiedException("İstifadəçi artıq təsdiqlənib.");

        // Qeydiyyat OTP-si üçün false
        sendOrUpdateOtp(user, false);
    }

    @Override
    @Transactional
    public void forgotPassword(ForgotPasswordRequestDTO request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new UserNotFoundException("İstifadəçi tapılmadı."));

        // Şifrə sıfırlama olduğu üçün true
        sendOrUpdateOtp(user, true);
    }

    @Override
    @Transactional
    public void resendForgotPasswordOtp(ResendRequestDTO request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new UserNotFoundException("İstifadəçi tapılmadı."));

        // Şifrə sıfırlama üçün true
        sendOrUpdateOtp(user, true);
    }

    // Əsas OTP göndərmə məntiqi (Dinamik mesajla)
    private void sendOrUpdateOtp(User user, boolean isResetPassword) {
        VerificationToken token = tokenRepository.findByUser(user)
                .orElseGet(() -> {
                    VerificationToken newToken = new VerificationToken();
                    newToken.setUser(user);
                    return newToken;
                });

        String otpCode = generateOtp();
        token.setToken(otpCode);
        token.setExpiryDate(LocalDateTime.now().plusMinutes(5));
        tokenRepository.save(token);

        String subject;
        String content;

        if (isResetPassword) {
            subject = "Smart Score - Şifrə Sıfırlama Kodu";
            content = "Şifrənizi yeniləmək üçün aşağıdakı təsdiq kodunu istifadə edin:";
        } else {
            subject = "Smart Score - Email Təsdiqi Kodu (OTP)";
            content = "Qeydiyyatınızı tamamlamaq üçün aşağıdakı təsdiq kodunu istifadə edin:";
        }

        mailService.sendOtpEmail(user.getEmail(), otpCode, subject, content);
    }

    @Override
    @Transactional
    public AuthResponseDTO verifyUser(VerifyRequestDTO request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new UserNotFoundException("İstifadəçi tapılmadı."));

        VerificationToken verificationToken = tokenRepository.findByUser(user)
                .orElseThrow(() -> new InvalidOtpException("Təsdiq kodu tapılmadı."));

        if (verificationToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            tokenRepository.delete(verificationToken);
            throw new OtpExpiredException("OTP kodunun vaxtı bitib.");
        }

        if (!request.getOtpCode().equals(verificationToken.getToken())) {
            throw new InvalidOtpException("Daxil etdiyiniz OTP kodu yanlışdır.");
        }

        user.setVerified(true);
        userRepository.save(user);
        tokenRepository.delete(verificationToken);

        return new AuthResponseDTO(true, "Email uğurla təsdiqləndi.");
    }

    @Override
    @Transactional
    public AuthResponseDTO loginUser(LoginRequestDTO request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new UserNotFoundException("Email və ya şifrə yanlışdır."));

        // Şifrəni yoxlayırıq ki, hər kəsə boş-boşuna OTP getməsin
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new InvalidCredentialsException("Email və ya şifrə yanlışdır.");
        }

        // Əgər təsdiqlənməyibsə:
        if (!user.isVerified()) {
            sendOrUpdateOtp(user, false); // Bu metod OTP-ni bazada yeniləyir və mail göndərir

            // BU HİSSƏ ƏSASDIR: 'throw' əvəzinə 'return' istifadə edirik
            // Beləliklə bazadakı yeni OTP 'rollback' olunmur, yadda qalır.
            return new AuthResponseDTO(false, "Email təsdiqi tələb olunur. Yeni OTP göndərildi.");
        }

        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(user.getEmail(), request.getPassword())
            );
            return new AuthResponseDTO(true, "Giriş uğurludur.",
                    jwtService.generateToken(user), jwtService.generateRefreshToken(user));
        } catch (AuthenticationException e) {
            throw new InvalidCredentialsException("Email və ya şifrə yanlışdır.");
        }
    }

    @Override
    @Transactional
    public void logout(String authHeader, String refreshToken) {
        if (refreshToken == null || refreshToken.trim().isEmpty()) {
            throw new InvalidTokenException("Refresh Token tələb olunur.");
        }

        jwtService.findUsername(refreshToken);

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            saveRevokedToken(authHeader.substring(7).trim());
        }

        saveRevokedToken(refreshToken.trim());
    }

    private void saveRevokedToken(String token) {
        if (!revokedTokenRepository.existsByToken(token)) {
            RevokedToken rt = new RevokedToken();
            rt.setToken(token);
            rt.setRevokedAt(LocalDateTime.now());
            revokedTokenRepository.save(rt);
        }
    }

    @Override
    @Transactional
    public AuthResponseDTO resetPassword(ResetPasswordRequestDTO request) {
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new InvalidCredentialsException("Şifrələr uyğun deyil.");
        }
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new UserNotFoundException("İstifadəçi tapılmadı."));

        VerificationToken vt = tokenRepository.findByUser(user)
                .orElseThrow(() -> new InvalidOtpException("Kod tapılmadı."));

        if (!request.getOtpCode().equals(vt.getToken())) throw new InvalidOtpException("Kod yanlışdır.");

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
        tokenRepository.delete(vt);

        return new AuthResponseDTO(true, "Şifrə uğurla yeniləndi.");
    }

    @Override
    @Transactional
    public AuthResponseDTO loginWithGoogle(GoogleLoginRequestDTO request) {
        try {
            GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(new NetHttpTransport(), new GsonFactory())
                    .setAudience(Collections.singletonList(googleClientId))
                    .build();

            GoogleIdToken idToken = verifier.verify(request.getIdToken());
            if (idToken == null) throw new InvalidTokenException("Google tokeni yanlışdır.");

            GoogleIdToken.Payload payload = idToken.getPayload();
            String email = payload.getEmail();

            User user = userRepository.findByEmail(email).orElseGet(() -> {
                User newUser = new User();
                newUser.setEmail(email);
                newUser.setFullName((String) payload.get("name"));
                newUser.setPassword(passwordEncoder.encode(UUID.randomUUID().toString()));
                newUser.setRole(Role.USER);
                newUser.setVerified(true);
                return userRepository.save(newUser);
            });

            return new AuthResponseDTO(true, "Google giriş uğurludur.",
                    jwtService.generateToken(user), jwtService.generateRefreshToken(user));
        } catch (Exception e) {
            throw new RuntimeException("Google xətası: " + e.getMessage());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public AuthResponseDTO getUserProfile(String email) {
        User user = userRepository.findByEmail(email).orElseThrow(() -> new UserNotFoundException("Tapılmadı."));
        AuthResponseDTO resp = new AuthResponseDTO(true, "Uğurlu.");
        resp.setFullName(user.getFullName());
        resp.setEmail(user.getEmail());
        resp.setCreatedAt(user.getCreatedAt());
        return resp;
    }

    @Override
    @Transactional
    public AuthResponseDTO refreshToken(String refreshToken) {
        if (revokedTokenRepository.existsByToken(refreshToken)) {
            throw new InvalidTokenException("Bu Refresh Token ləğv edilib. Yenidən giriş edin.");
        }

        String email = jwtService.findUsername(refreshToken);
        User user = userRepository.findByEmail(email).orElseThrow(() -> new UserNotFoundException("Tapılmadı."));

        return new AuthResponseDTO(true, "Yeniləndi.",
                jwtService.generateToken(user), jwtService.generateRefreshToken(user));
    }

    private String generateOtp() {
        return String.format("%04d", new Random().nextInt(10000));
    }
}