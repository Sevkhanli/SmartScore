package az.edu.itbrains.SmartScore.dtos.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class HeroRequestDTO {
    private String mainTitle;      // Məs: "Kredit ehtimalınızı öncədən bilin"
    private String subtitle;       // Məs: "Pulsuz və Sürətli Qiymətləndirmə"
    private String description;    // Məs: "Bank çıxarışınızı yükləyin və..."
    private String buttonText;     // Məs: "İndi başla"
}
