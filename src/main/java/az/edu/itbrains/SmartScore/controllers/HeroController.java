package az.edu.itbrains.SmartScore.controllers;

import lombok.RequiredArgsConstructor;
import az.edu.itbrains.SmartScore.dtos.request.HeroRequestDTO;
import az.edu.itbrains.SmartScore.dtos.response.HeroResponseDTO;
import az.edu.itbrains.SmartScore.services.HeroService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/hero")
@RequiredArgsConstructor
public class HeroController {

    private final HeroService heroService;

    // Ana səhifədə hər kəsin görəcəyi aktiv Hero məlumatı
    @GetMapping("/active")
    public ResponseEntity<HeroResponseDTO> getActiveHero() {
        return ResponseEntity.ok(heroService.getActiveHero());
    }

    // Admin tərəfindən yeni Hero mətni yaratmaq
    @PostMapping("/admin")
    public ResponseEntity<HeroResponseDTO> createHero(@RequestBody HeroRequestDTO heroRequestDTO) {
        return new ResponseEntity<>(heroService.createHero(heroRequestDTO), HttpStatus.CREATED);
    }

    // Mövcud Hero mətnini id-yə görə yeniləmək
    @PutMapping("/admin/{id}")
    public ResponseEntity<HeroResponseDTO> updateHero(
            @PathVariable long id,
            @RequestBody HeroRequestDTO heroRequestDTO) {
        return ResponseEntity.ok(heroService.updateHero(id, heroRequestDTO));
    }
}