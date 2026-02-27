package az.edu.itbrains.SmartScore.services.impls;

import az.edu.itbrains.SmartScore.dtos.request.*;
import az.edu.itbrains.SmartScore.dtos.response.AuthResponseDTO;
import az.edu.itbrains.SmartScore.enums.Role;
import az.edu.itbrains.SmartScore.exceptions.*;
import az.edu.itbrains.SmartScore.models.RevokedToken;
import az.edu.itbrains.SmartScore.models.User;
import az.edu.itbrains.SmartScore.models.VerificationToken;
import az.edu.itbrains.SmartScore.repositories.RevokedTokenRepository;
import az.edu.itbrains.SmartScore.repositories.UserRepository;
import az.edu.itbrains.SmartScore.repositories.VerificationTokenRepository;
import az.edu.itbrains.SmartScore.security.JwtService;
import az.edu.itbrains.SmartScore.services.MailService;
import az.edu.itbrains.SmartScore.services.UserService;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
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

    // --- RAMILIN METODLARI (SADELEŞDIRILMIŞ VƏ OPTIONAL-A UYĞUNLAŞDIRILMIŞ) ---

    @Override
    public User findByTelegramChatId(Long chatId) {
        return userRepository.findByTelegramChatId(chatId).orElse(null);
    }

    @Override
    @Transactional
    public boolean linkTelegram(String email, Long chatId) {
        return userRepository.findByEmail(email).map(user -> {
            user.setTelegramChatId(chatId);
            userRepository.save(user);
            return true;
        }).orElse(false);
    }

    @Override
    public User getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email).orElse(null);
    }

    // --- SƏNIN METODLARIN (AUTH MƏNTİQİ) ---

    @Override
    @Transactional
    public AuthResponseDTO registerUser(RegisterRequestDTO request) {
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new InvalidCredentialsException("Şifrələr bir-biri ilə uyğun gəlmir.");
        }

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
        sendOrUpdateOtp(savedUser, false);

        return new AuthResponseDTO(true, "Qeydiyyat uğurludur. Email təsdiqi tələb olunur.");
    }

    @Override
    @Transactional
    public void resendOtp(ResendRequestDTO request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new UserNotFoundException("İstifadəçi tapılmadı."));

        if (user.isVerified()) throw new UserAlreadyVerifiedException("İstifadəçi artıq təsdiqlənib.");

        sendOrUpdateOtp(user, false);
    }

    @Override
    @Transactional
    public void forgotPassword(ForgotPasswordRequestDTO request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new UserNotFoundException("İstifadəçi tapılmadı."));

        sendOrUpdateOtp(user, true);
    }

    @Override
    @Transactional
    public void resendForgotPasswordOtp(ResendRequestDTO request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new UserNotFoundException("İstifadəçi tapılmadı."));

        sendOrUpdateOtp(user, true);
    }

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

        String subject = isResetPassword ? "Smart Score - Şifrə Sıfırlama Kodu" : "Smart Score - Email Təsdiqi Kodu (OTP)";
        String content = isResetPassword ? "Şifrənizi yeniləmək üçün kod:" : "Qeydiyyatınızı tamamlamaq üçün kod:";

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

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new InvalidCredentialsException("Email və ya şifrə yanlışdır.");
        }

        if (!user.isVerified()) {
            sendOrUpdateOtp(user, false);
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