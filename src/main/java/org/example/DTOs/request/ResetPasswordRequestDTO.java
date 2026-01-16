package org.example.DTOs.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ResetPasswordRequestDTO {
    @NotBlank(message = "Email boş ola bilməz")
    private String email;

    @NotBlank(message = "OTP kodu boş ola bilməz")
    private String otpCode;

    @NotBlank(message = "Yeni şifrə boş ola bilməz")
    @Size(min = 6, message = "Şifrə ən az 6 simvol olmalıdır")
    private String newPassword;

    @NotBlank(message = "Şifrə təkrarı boş ola bilməz")
    private String confirmPassword;
}