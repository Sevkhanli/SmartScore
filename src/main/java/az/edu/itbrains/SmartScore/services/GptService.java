package az.edu.itbrains.SmartScore.services;

import az.edu.itbrains.SmartScore.dtos.transaction.TransactionDto;

import java.util.List;

public interface GptService {
    List<TransactionDto> analyzeStatement(String extractedText);
}
