package az.edu.itbrains.SmartScore.controllers;

import az.edu.itbrains.SmartScore.dtos.request.ContactRequestDTO;
import az.edu.itbrains.SmartScore.services.ContactService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/contact")
@RequiredArgsConstructor
@CrossOrigin(origins = "*") // Frontend-dən gələn sorğulara icazə vermək üçün
public class ContactController {

    private final ContactService contactService;

    @PostMapping("/send")
    public ResponseEntity<String> sendFeedback(@Valid @RequestBody ContactRequestDTO requestDTO) {
        try {
            contactService.sendMessage(requestDTO);
            return ResponseEntity.ok("Müraciətiniz uğurla qeydə alındı və Telegram vasitəsilə  göndərildi.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Xəta baş verdi: " + e.getMessage());
        }
    }
}