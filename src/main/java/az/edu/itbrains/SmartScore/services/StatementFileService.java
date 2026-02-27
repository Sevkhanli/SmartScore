package az.edu.itbrains.SmartScore.services;

import az.edu.itbrains.SmartScore.dtos.statementFile.StatementFileDto;
import az.edu.itbrains.SmartScore.models.User;
import org.telegram.telegrambots.meta.api.objects.Document;
import org.telegram.telegrambots.meta.bots.AbsSender;

public interface StatementFileService    {
    void downloadAndSave(String fileId, String fileName, AbsSender bot);

    StatementFileDto save(Document doc, User user);
}
