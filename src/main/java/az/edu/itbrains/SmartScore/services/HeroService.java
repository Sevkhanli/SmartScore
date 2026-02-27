package az.edu.itbrains.SmartScore.services;

import az.edu.itbrains.SmartScore.dtos.request.HeroRequestDTO;
import az.edu.itbrains.SmartScore.dtos.response.HeroResponseDTO;

public interface HeroService {
    HeroResponseDTO getActiveHero();
    HeroResponseDTO updateHero(long id, HeroRequestDTO heroRequestDTO);
    HeroResponseDTO createHero(HeroRequestDTO heroRequestDTO);
//     void deleteHero(long id);
}
