package org.example.DTOs.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegisterRequestDTO {

    @NotBlank(message = "Ad və soyad boş ola bilməz")
    @Size(min = 3, max = 100, message = "Ad və soyad 3-100 simvol arasında olmalıdır")
    private String fullName;

    @NotBlank(message = "Email boş ola bilməz")
    @Email(message = "Email düzgün formatda deyil")
    private String email;

    @NotBlank(message = "Şifrə boş ola bilməz")
    @Size(min = 8, max = 50, message = "Şifrə minimum 8 simvol olmalıdır")
    @Pattern(
            regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).*$",
            message = "Şifrədə ən az bir böyük hərf, bir kiçik hərf və bir rəqəm olmalıdır"
    )
    private String password;

    // Yeni əlavə olunan sahə
    @NotBlank(message = "Şifrə təsdiqi boş ola bilməz")
    private String confirmPassword;
}
