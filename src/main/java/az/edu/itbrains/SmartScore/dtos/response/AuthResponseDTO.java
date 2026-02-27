package az.edu.itbrains.SmartScore.dtos.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude; // 1. BU İMPORTU ƏLAVƏ EDİN
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

// 2. BU ANOTASİYANI ƏLAVƏ EDİN
@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
@NoArgsConstructor
public class AuthResponseDTO {

    private Boolean success;
    private String message;
    // Bu sahələr null olduqda JSON-a düşməyəcək
    private String accessToken;
    private String refreshToken;

    //TODO Profil məlumatları üçün sahələr
    private String fullName;
    private String email;
    @JsonFormat(pattern = "dd.MM.yyyy") // Məsələn: 16.01.2026
    private LocalDateTime createdAt;

    // 1. Register, Verify və ya xəta üçün konstruktor (tokenlər null olacaq)
    public AuthResponseDTO(Boolean success, String message) {
        this.success = success;
        this.message = message;
        this.accessToken = null;
        this.refreshToken = null;
    }

    // 2. Login və Refresh üçün konstruktor (tokenlər dolu olacaq)
    public AuthResponseDTO(Boolean success, String message, String accessToken, String refreshToken) {
        this.success = success;
        this.message = message;
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
    }
}