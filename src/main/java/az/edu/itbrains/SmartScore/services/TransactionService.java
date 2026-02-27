package az.edu.itbrains.SmartScore.services;

import az.edu.itbrains.SmartScore.dtos.transaction.TransactionDto;
import az.edu.itbrains.SmartScore.models.User;

import java.util.List;

public interface TransactionService {
    void createTransactionsFromAi(List<TransactionDto> aiAnalysis, User user, Long id);
}
