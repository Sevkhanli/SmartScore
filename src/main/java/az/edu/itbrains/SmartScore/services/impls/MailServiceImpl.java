package az.edu.itbrains.SmartScore.services.impls;

import az.edu.itbrains.SmartScore.dtos.emailMessage.EmailMessage;
import az.edu.itbrains.SmartScore.services.MailService;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MailServiceImpl implements MailService {

    private final JavaMailSender mailSender;

    /**
     * RabbitMQ-dan gələn mesajları dinləyən metod.
     * "email_queue" növbəsinə mesaj düşən kimi bura işə düşür.
     */
    @RabbitListener(queues = "email_queue")
    public void receiveEmailMessage(EmailMessage emailData) {
        System.out.println("DEBUG: RabbitMQ-dan mesaj gəldi. Göndərilir: " + emailData.getToEmail());

        // Həqiqi mail göndərmə metodunu çağırırıq
        sendOtpEmail(
                emailData.getToEmail(),
                emailData.getOtpCode(),
                emailData.getSubject(),
                emailData.getMessageContent()
        );
    }

    @Override
    public void sendOtpEmail(String toEmail, String otpCode, String subject, String messageContent) {
        SimpleMailMessage message = new SimpleMailMessage();

        message.setFrom("sevxanli77@gmail.com");
        message.setTo(toEmail);
        message.setSubject(subject);

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