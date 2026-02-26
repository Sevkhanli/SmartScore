package org.example.DTOs.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.checkerframework.checker.units.qual.A;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class HeroResponseDTO {
    private Long id;
    private String mainTitle;      // Məs: "Kredit ehtimalınızı öncədən bilin"
    private String subtitle;       // Məs: "Pulsuz və Sürətli Qiymətləndirmə"
    private String description;    // Məs: "Bank çıxarışınızı yükləyin və..."
    private String buttonText;     // Məs: "İndi başla"
    private boolean active;       // Hansı məlumatın hal-hazırda görünəcəyini seçmək üçün
}
