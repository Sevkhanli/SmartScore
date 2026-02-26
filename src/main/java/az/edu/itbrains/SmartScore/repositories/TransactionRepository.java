package az.edu.itbrains.SmartScore.repositories;

import az.edu.itbrains.SmartScore.models.StatementFile;
import az.edu.itbrains.SmartScore.models.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface TransactionRepository extends JpaRepository<Transaction,Long> {
   // List<Transaction> findAllByUserIdAndOperationDateAfter(Long id, LocalDateTime startDate);

  //  List<Transaction> findAllByUserId(Long id);

    List<Transaction> findAllByStatementFileId(Long id);

    void deleteByStatementFile(StatementFile file);
}
