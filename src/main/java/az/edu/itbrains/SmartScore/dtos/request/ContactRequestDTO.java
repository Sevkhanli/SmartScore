package az.edu.itbrains.SmartScore.dtos.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ContactRequestDTO {

    @NotBlank(message = "Ad və Soyad boş ola bilməz")
    private String fullName;

    @Email(message = "Düzgün email daxil edin")
    @NotBlank(message = "Email boş ola bilməz")
    private String email;

    @NotBlank(message = "Mesaj boş ola bilməz")
    @Size(min = 10, max = 1000, message = "Mesaj 10 ilə 1000 simvol arası olmalıdır")
    private String message;
}