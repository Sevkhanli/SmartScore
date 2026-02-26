package org.example.services;

import org.example.DTOs.request.HeroRequestDTO;
import org.example.DTOs.response.HeroResponseDTO;

public interface HeroService {
    HeroResponseDTO getActiveHero();
    HeroResponseDTO updateHero(long id, HeroRequestDTO heroRequestDTO);
    HeroResponseDTO createHero(HeroRequestDTO heroRequestDTO);
//     void deleteHero(long id);
}
