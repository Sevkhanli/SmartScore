package org.example.repositories;

import org.example.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    Boolean existsByEmail(String email);
//
//    Optional<User> findByPhoneNumber(String phoneNumber);
//    Boolean existsByPhoneNumber(String phoneNumber);
    Optional<User> findByEmailAndIsVerifiedFalse(String email);
}