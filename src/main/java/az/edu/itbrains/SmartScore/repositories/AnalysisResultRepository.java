package az.edu.itbrains.SmartScore.repositories;

import az.edu.itbrains.SmartScore.models.AnalysisResult;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface AnalysisResultRepository extends JpaRepository<AnalysisResult, Long> {

    // 1. Ən sonuncu tək analizi tapmaq üçün (Mövcuddur, düzgündür)
    Optional<AnalysisResult> findTopByUserIdOrderByCalculatedAtDesc(Long userId);

    // 2. İstifadəçinin BÜTÜN analizlərini tarixinə görə sıralı gətirmək üçün (YENİ ƏLAVƏ ET)
    // Bu metod AnalysisResultServiceImpl-dakı "history" siyahısını doldurmaq üçün lazımdır.
    List<AnalysisResult> findAllByUserIdOrderByCalculatedAtDesc(Long userId);
}