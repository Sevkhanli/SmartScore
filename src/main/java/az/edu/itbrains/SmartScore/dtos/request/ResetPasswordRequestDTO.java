package az.edu.itbrains.SmartScore.dtos.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
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
    @Size(min = 8, max = 50, message = "Şifrə ən az 8 simvol olmalıdır")
    @Pattern(
            regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).*$",
            message = "Şifrədə ən az bir böyük hərf, bir kiçik hərf və bir rəqəm olmalıdır"
    )
    private String newPassword;

    @NotBlank(message = "Şifrə təkrarı boş ola bilməz")
    private String confirmPassword;
}