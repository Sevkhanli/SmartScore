package org.example.repositories;

import org.example.entities.RevokedToken;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface RevokedTokenRepository extends JpaRepository<RevokedToken, Long> {
    Optional<RevokedToken> findByToken(String token);
    boolean existsByToken(String token);
}