package az.edu.itbrains.SmartScore.dtos.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResendRequestDTO {
    @NotBlank(message = "Email boş ola bilməz")
    @Email(message = "Email düzgün formatda deyil")
    private String email;
}
