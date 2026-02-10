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
    public void sendOtpEmail(String toEmail, String otpCode, String subject, String messageContent) {
        SimpleMailMessage message = new SimpleMailMessage();

        // SendGrid və ya digər provayderdədə təsdiqlənmiş göndərən mail
        message.setFrom("sevxanli77@gmail.com");
        message.setTo(toEmail);

        // Dinamik başlıq (subject)
        message.setSubject(subject);

        // Dinamik məzmun (messageContent)
        String body = String.format("""
            Hörmətli istifadəçi,

            %s

            OTP Kodu: %s

            Qeyd: Bu kod 5 dəqiqə ərzində etibarlıdır.

            Hörmətlə,
            Smart Score Komandası
            """, messageContent, otpCode);

        message.setText(body);

        try {
            mailSender.send(message);
            System.out.println("SUCCESS: " + subject + " kodu " + toEmail + " ünvanına uğurla göndərildi.");
        } catch (Exception e) {
            System.err.println("ERROR: Mail göndərilərkən xəta baş verdi: " + e.getMessage());
        }
    }
}