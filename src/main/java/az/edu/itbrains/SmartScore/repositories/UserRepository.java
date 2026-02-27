package az.edu.itbrains.SmartScore.repositories;

import az.edu.itbrains.SmartScore.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    // Telegram üçün
    Optional<User> findByTelegramChatId(Long telegramChatId);

    // Email üçün (Optional istifadə etmək ən təhlükəsizidir)
    Optional<User> findByEmail(String email);

    // Email mövcudluğunu yoxlamaq üçün
    Boolean existsByEmail(String email);

    // Təsdiqlənməmiş istifadəçini tapmaq üçün
    Optional<User> findByEmailAndVerifiedFalse(String email);
}