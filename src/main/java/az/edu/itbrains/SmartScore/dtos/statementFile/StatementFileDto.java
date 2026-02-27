package az.edu.itbrains.SmartScore.dtos.statementFile;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class StatementFileDto {
    private Long id;

    private String originalFileName;

    private String storedFilePath;

    private String fileType;

    private LocalDateTime uploadedAt;
}
