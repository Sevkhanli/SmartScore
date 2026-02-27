package az.edu.itbrains.SmartScore.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "hero_content")
public class HeroContent {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String mainTitle;      // Məs: "Kredit ehtimalınızı öncədən bilin"
    private String subtitle;       // Məs: "Pulsuz və Sürətli Qiymətləndirmə"
    private String description;    // Məs: "Bank çıxarışınızı yükləyin və..."
    private String buttonText;     // Məs: "İndi başla"

    private boolean active = true; // Hansı məlumatın hal-hazırda görünəcəyini seçmək üçün
}

