package az.edu.itbrains.SmartScore.repositories;

import az.edu.itbrains.SmartScore.models.RevokedToken;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface RevokedTokenRepository extends JpaRepository<RevokedToken, Long> {
    Optional<RevokedToken> findByToken(String token);
    boolean existsByToken(String token);
}