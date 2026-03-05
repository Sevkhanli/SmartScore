package az.edu.itbrains.SmartScore.services.impls;
import az.edu.itbrains.SmartScore.dtos.request.ContactRequestDTO;
import az.edu.itbrains.SmartScore.models.ContactMessage;
import az.edu.itbrains.SmartScore.repositories.ContactMessageRepository;
import az.edu.itbrains.SmartScore.services.ContactService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class ContactServiceImpl implements ContactService {

    private final ContactMessageRepository repository;

    // Spring-in RestTemplate-i ilə kənar API-a (Telegram) müraciət edəcəyik
    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${telegram.bot.token}")
    private String botToken;

    @Value("${telegram.chat.id}")
    private String chatId;

    @Override
    public void sendMessage(ContactRequestDTO requestDTO) {
        // 1. Məlumatı bazaya qeyd edirik
        ContactMessage contactMessage = ContactMessage.builder()
                .fullName(requestDTO.getFullName())
                .email(requestDTO.getEmail())
                .message(requestDTO.getMessage())
                .createdAt(LocalDateTime.now())
                .build();

        repository.save(contactMessage);

        // 2. Telegram Botuna bildiriş göndəririk
        sendToTelegram(requestDTO);
    }

    private void sendToTelegram(ContactRequestDTO dto) {
        String messageText = String.format(
                "📩 *Yeni Şikayət və ya Təklif*\n\n" +
                        "👤 *Ad Soyad:* %s\n" +
                        "📧 *Email:* %s\n" +
                        "📝 *Mesaj:* %s",
                dto.getFullName(), dto.getEmail(), dto.getMessage()
        );

        // Telegram API URL formatı
        String url = "https://api.telegram.org/bot" + botToken +
                "/sendMessage?chat_id=" + chatId +
                "&text=" + messageText +
                "&parse_mode=Markdown";

        try {
            // Telegram-a GET sorğusu atırıq
            restTemplate.getForObject(url, String.class);
        } catch (Exception e) {
            // Əgər Telegram-da nəsə səhv getsə, müraciət bazada qaldığı üçün proqramı dayandırmırıq
            System.err.println("Telegram-a mesaj göndərilərkən xəta: " + e.getMessage());
        }
    }
}