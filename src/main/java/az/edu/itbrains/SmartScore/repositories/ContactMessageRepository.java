package az.edu.itbrains.SmartScore.repositories;

import az.edu.itbrains.SmartScore.models.ContactMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ContactMessageRepository extends JpaRepository<ContactMessage, Long> {
}