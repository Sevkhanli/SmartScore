package az.edu.itbrains.SmartScore.services.impls;

import az.edu.itbrains.SmartScore.dtos.statementFile.StatementFileDto;
import az.edu.itbrains.SmartScore.enums.StatementFileStatus;
import az.edu.itbrains.SmartScore.models.StatementFile;
import az.edu.itbrains.SmartScore.models.User;
import az.edu.itbrains.SmartScore.repositories.StatementFileRepository;
import az.edu.itbrains.SmartScore.services.StatementFileService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.GetFile;
import org.telegram.telegrambots.meta.api.objects.Document;
import org.telegram.telegrambots.meta.api.objects.File;
import org.telegram.telegrambots.meta.bots.AbsSender;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class StatementFileServiceImpl implements StatementFileService {

    private final StatementFileRepository statementFileRepository;
    private final ModelMapper mapper;

    @Override
    public StatementFileDto save(Document telegramDoc, User user) {
        StatementFile file = new StatementFile();
        file.setOriginalFileName(telegramDoc.getFileName());
        file.setStoredFilePath(telegramDoc.getFileId());
        file.setFileType("pdf");
        file.setUploadedAt(LocalDateTime.now());
        file.setStatus(StatementFileStatus.UPLOADED);
        file.setUser(user);

        StatementFile savedEntity = statementFileRepository.save(file);

        return mapper.map(savedEntity, StatementFileDto.class);
    }

    @Override
    public void downloadAndSave(String fileId, String fileName, AbsSender bot) {
        try {
            GetFile getFile = new GetFile();
            getFile.setFileId(fileId);
            File file = bot.execute(getFile);
            java.io.File localFile = new java.io.File("uploads/" + fileName);
            localFile.getParentFile().mkdirs();

            if (bot instanceof TelegramLongPollingBot) {
                ((TelegramLongPollingBot) bot).downloadFile(file, localFile);
            }
            System.out.println("Fayl uğurla endirildi!");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}