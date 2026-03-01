package az.edu.itbrains.SmartScore.dtos.emailMessage;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EmailMessage implements Serializable {
    private String toEmail;
    private String otpCode;
    private String subject;
    private String messageContent;
}