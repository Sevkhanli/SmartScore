package org.example.services.Impl;

import lombok.RequiredArgsConstructor;
import org.example.services.MailService;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MailServiceImpl implements MailService {

    private final JavaMailSender mailSender;


    @Override
    @Async
    public void sendOtpEmail(String toEmail, String otpCode) {
        SimpleMailMessage message = new SimpleMailMessage();

        message.setTo(toEmail);

        message.setSubject("Smart Score - Email Təsdiqi Kodu (OTP)");

        String body = String.format("""
            Hörmətli istifadəçi,

            Qeydiyyatınızı tamamlamaq üçün aşağıdakı təsdiq kodunu istifadə edin:

            OTP Kodu: %s

            Qeyd: Bu kod 5 dəqiqə ərzində etibarlıdır.

            Hörmətlə,
            Smart Score Komandası
            """, otpCode);

        message.setText(body);

        try {
            mailSender.send(message);
            System.out.println("SUCCESS: OTP kodu " + toEmail + " ünvanına uğurla göndərildi.");
        } catch (Exception e) {
            System.err.println("ERROR: Mail göndərilərkən xəta baş verdi: " + e.getMessage());
        }
    }
}