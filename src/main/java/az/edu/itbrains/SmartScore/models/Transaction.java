package az.edu.itbrains.SmartScore.models;

import az.edu.itbrains.SmartScore.enums.CategoryType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Date;

@Data
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "transactions")
public class Transaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private BigDecimal amount;

    private String description;

    private Double confidence;

    private Date operationDate;

    @Enumerated(EnumType.STRING)
    private CategoryType category;

    @ManyToOne(optional = false)
    @JoinColumn(name = "statement_file_id")
    private StatementFile statementFile;

    @ManyToOne(optional = false)
    private User user;
}
