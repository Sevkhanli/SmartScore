package az.edu.itbrains.SmartScore.repositories;

import az.edu.itbrains.SmartScore.models.HeroContent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface HeroRepository extends JpaRepository <HeroContent, Long> {
    Optional<HeroContent> findByActiveTrue();
}
