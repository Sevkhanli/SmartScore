package az.edu.itbrains.SmartScore.services.impls;

import az.edu.itbrains.SmartScore.dtos.transaction.TransactionDto;
import az.edu.itbrains.SmartScore.enums.CategoryType;
import az.edu.itbrains.SmartScore.models.StatementFile;
import az.edu.itbrains.SmartScore.models.Transaction;
import az.edu.itbrains.SmartScore.models.User;
import az.edu.itbrains.SmartScore.repositories.StatementFileRepository;
import az.edu.itbrains.SmartScore.repositories.TransactionRepository;
import az.edu.itbrains.SmartScore.services.TransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.SimpleDateFormat;
import java.util.*;

@Service
@RequiredArgsConstructor
public class TransactionServiceImpl implements TransactionService {

    private final TransactionRepository transactionRepository;
    private final StatementFileRepository statementFileRepository;

    @Override
    @Transactional
    public void createTransactionsFromAi(List<TransactionDto> dtos, User user, Long fileId) {

        StatementFile file = statementFileRepository.findById(fileId)
                .orElseThrow(() -> new RuntimeException("File not found"));

        // 1. Очистка старых транзакций этого файла
        transactionRepository.deleteByStatementFile(file);

        List<Transaction> transactions = new ArrayList<>();

        // ✅ ИСПРАВЛЕНИЕ: Добавлен формат с точным временем от GPT
        SimpleDateFormat fmtIso = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        SimpleDateFormat fmt1 = new SimpleDateFormat("yyyy-MM-dd");
        SimpleDateFormat fmt2 = new SimpleDateFormat("dd.MM.yyyy");

        for (TransactionDto dto : dtos) {
            if (dto.getOperationDate() == null || dto.getOperationDate().isEmpty()) {
                continue;
            }

            Date opDate = null;
            try {
                // Сначала пробуем вытащить ПОЛНУЮ дату с временем
                opDate = fmtIso.parse(dto.getOperationDate());
            } catch (Exception e0) {
                try {
                    opDate = fmt1.parse(dto.getOperationDate());
                } catch (Exception e1) {
                    try {
                        opDate = fmt2.parse(dto.getOperationDate());
                    } catch (Exception e2) {
                        continue;
                    }
                }
            }

            Transaction t = new Transaction();
            t.setUser(user);
            t.setStatementFile(file);
            t.setAmount(dto.getAmount());
            t.setDescription(dto.getDescription());
            t.setOperationDate(opDate);

            CategoryType cat = dto.getCategory() != null ? dto.getCategory() : CategoryType.OTHER;
            t.setCategory(cat);
            t.setConfidence(dto.getConfidence() != null ? dto.getConfidence() : 0.7);

            transactions.add(t);
        }

        transactionRepository.saveAll(transactions);
    }
}