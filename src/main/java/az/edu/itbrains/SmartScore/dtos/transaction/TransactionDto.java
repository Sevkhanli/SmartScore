package az.edu.itbrains.SmartScore.dtos.transaction;

import az.edu.itbrains.SmartScore.enums.CategoryType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TransactionDto {
    private BigDecimal amount;
    private String description;
    private Double confidence;
    private CategoryType category;
    private String operationDate;
}