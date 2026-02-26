package org.example.services.Impl;

import lombok.RequiredArgsConstructor;
import org.example.DTOs.request.HeroRequestDTO;
import org.example.DTOs.response.HeroResponseDTO;
import org.example.entities.HeroContent;
import org.example.repositories.HeroRepository;
import org.example.services.HeroService;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class HeroServiceImpl implements HeroService {

    private final HeroRepository heroRepository;
    private final ModelMapper modelMapper;

    @Override
    public HeroResponseDTO getActiveHero() {
        HeroContent hero = heroRepository.findByActiveTrue()
                .orElseThrow(() -> new RuntimeException("Aktiv hero tapılmadı"));
        return modelMapper.map(hero, HeroResponseDTO.class);
    }

    @Override
    public HeroResponseDTO createHero(HeroRequestDTO heroRequestDTO) {
        HeroContent hero = modelMapper.map(heroRequestDTO, HeroContent.class);
        hero.setActive(true);

        HeroContent saved = heroRepository.save(hero);
        return modelMapper.map(saved, HeroResponseDTO.class);
    }

    @Override
    public HeroResponseDTO updateHero(long id, HeroRequestDTO heroRequestDTO) {
        // 1. Köhnə məlumatı bazadan tapırıq
        HeroContent hero = heroRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Hero tapılmadı"));

        // 2. DTO-dakı yeni məlumatları bazadan gələn 'hero' obyektinin üzərinə yazırıq
        modelMapper.map(heroRequestDTO, hero);

        // 3. Yenilənmiş obyekti yadda saxlayırıq
        HeroContent updated = heroRepository.save(hero);

        // 4. Yenilənmiş obyekti DTO-ya çevirib geri qaytarırıq
        return modelMapper.map(updated, HeroResponseDTO.class);
    }

}
