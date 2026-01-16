package org.example.DTOs.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ForgotPasswordRequestDTO {
    @Email(message = "Düzgün email daxil edin")
    @NotBlank(message = "Email boş ola bilməz")
    private String email;
}