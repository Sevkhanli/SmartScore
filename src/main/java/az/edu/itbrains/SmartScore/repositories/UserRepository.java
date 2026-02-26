package az.edu.itbrains.SmartScore.repositories;

import az.edu.itbrains.SmartScore.models.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByTelegramChatId(Long telegramChatId);

    User findByEmail(String email);
}
