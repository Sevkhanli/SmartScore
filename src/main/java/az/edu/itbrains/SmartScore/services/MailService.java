package az.edu.itbrains.SmartScore.services;

public interface MailService {
    void sendOtpEmail(String toEmail, String otpCode, String subject, String messageContent);}