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
    private String fullName; // Ad və soyad



    @NotBlank(message = "Email boş ola bilməz")
    @Email(message = "Email düzgün formatda deyil")
    private String email;

    @NotBlank(message = "Şifrə boş ola bilməz")
    @Size(min = 6, max = 50, message = "Şifrə minimum 6 simvol olmalıdır")
    private String password;
}