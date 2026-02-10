package org.example.services;

public interface MailService {
    void sendOtpEmail(String toEmail, String otpCode, String subject, String messageContent);}