package org.example.repositories;

import org.example.entities.HeroContent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface HeroRepository extends JpaRepository <HeroContent, Long> {
    Optional<HeroContent> findByActiveTrue();
}
