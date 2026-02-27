package az.edu.itbrains.SmartScore.repositories;

import az.edu.itbrains.SmartScore.models.StatementFile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface StatementFileRepository extends JpaRepository <StatementFile, Long>{
    Optional<StatementFile> findTopByUserIdOrderByUploadedAtDesc(Long id);
}
