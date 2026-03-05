package az.edu.itbrains.SmartScore.services;


import az.edu.itbrains.SmartScore.dtos.request.ContactRequestDTO;

public interface ContactService {
    void sendMessage(ContactRequestDTO requestDTO);
}