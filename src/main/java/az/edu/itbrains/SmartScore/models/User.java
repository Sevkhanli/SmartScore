package az.edu.itbrains.SmartScore.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import az.edu.itbrains.SmartScore.enums.Role;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;

@Data
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String fullName;

    private Long telegramChatId;

    @Email(message = "email duzgun deyil")
    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @Column(name = "password", nullable = false)
    private String password;

    // DÜZƏLİŞ BURADADIR:
    // Bazadakı sütun adı 'is_verified' ola bilər, onu dəqiqləşdiririk və default dəyər veririk.
    @Column(name = "is_verified", nullable = false, columnDefinition = "boolean default false")
    private boolean verified = false;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    private Role role;
}