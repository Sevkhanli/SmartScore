package az.edu.itbrains.SmartScore.services.impls;

import az.edu.itbrains.SmartScore.dtos.analysisResult.AnalysisResultDto;
import az.edu.itbrains.SmartScore.dtos.statementFile.StatementFileDto;
import az.edu.itbrains.SmartScore.dtos.transaction.TransactionDto;
import az.edu.itbrains.SmartScore.services.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import az.edu.itbrains.SmartScore.models.User;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TelegramBotServiceImpl extends TelegramLongPollingBot {

    private final UserService userService;
    private final StatementFileService statementFileService;
    private final PdfService pdfService;
    private final GptService gptService;
    private final TransactionService transactionService;
    private final AnalysisResultService analysisResultService;

    private final ExecutorService analysisExecutor = Executors.newSingleThreadExecutor();

    @Value("${telegram.bot.username}")
    private String botUsername;
    @Value("${telegram.bot.token}")
    private String botToken;

    @Override
    public String getBotUsername() {
        return this.botUsername;
    }

    @Override
    public String getBotToken() {
        return this.botToken;
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (!update.hasMessage()) return;

        Long chatId = update.getMessage().getChatId();

        if (update.getMessage().hasText()) {
            String text = update.getMessage().getText();
            if (text.contains("@")) {
                boolean isLinked = userService.linkTelegram(text, chatId);
                if (isLinked) {
                    sendMsg(chatId, "Uğurla bağlandı! İndi bank çıxarışını (PDF) göndərə bilərsiniz. ✅");
                } else {
                    sendMsg(chatId, "Xəta: Bu email ilə sistemdə istifadəçi tapılmadı. ❌");
                }
                return;
            }
        }

        User user = userService.findByTelegramChatId(chatId);
        if (user == null) {
            sendMsg(chatId, "Sizi tanımadım. Zəhmət olmasa, qeydiyyatdan keçdiyiniz email ünvanını yazın.");
            return;
        }

        if (update.getMessage().hasDocument()) {
            var doc = update.getMessage().getDocument();

            sendMsg(chatId, "Fayl qəbul edildi. Növbəyə əlavə olundu, analiz üçün gözləyin... ⏳");

            analysisExecutor.submit(() -> {
                try {
                    processFileAnalysis(doc, user, chatId);
                } catch (Exception e) {
                    System.err.println("XƏTA: " + e.getMessage());
                    sendMsg(chatId, "Sistemdə xəta baş verdi. Zəhmət olmasa biraz sonra yenidən yoxlayın. 🛠");
                }
            });

        } else if (!update.getMessage().hasText()) {
            sendMsg(chatId, "Zəhmət olmasa bank çıxarışınızı PDF formatında göndərin.");
        }
    }

    /**
     * Вынесли тяжелую логику в отдельный метод для очереди
     */
    private void processFileAnalysis(org.telegram.telegrambots.meta.api.objects.Document doc, User user, Long chatId) throws Exception {
        System.out.println("1. Инфо о файле...");
        StatementFileDto savedFile = statementFileService.save(doc, user);

        System.out.println("2. Скачивание...");
        statementFileService.downloadAndSave(doc.getFileId(), doc.getFileName(), this);

        String filePath = "uploads/" + doc.getFileName();
        File file = new File(filePath);

        if (file.exists()) {
            System.out.println("3. Извлечение текста...");
            String rawText = pdfService.extractText(filePath);

            String cleanedText = cleanAndTrimText(rawText);

            System.out.println("4. Запрос к ИИ...");
            List<TransactionDto> aiAnalysis = gptService.analyzeStatement(cleanedText);
            if (aiAnalysis == null || aiAnalysis.isEmpty()) {
                sendMsg(chatId, "❌ İİ çıxarışdakı əməliyyatları tanıya bilmədi.");
                return;
            }

            System.out.println("5. Сохранение транзакций...");
            transactionService.createTransactionsFromAi(aiAnalysis, user, savedFile.getId());

            System.out.println("6. Расчет скоринга...");
            AnalysisResultDto scoreResult = analysisResultService.calculateScore(user);

            String responseText = String.format(
                    "✅ **Analiz tamamlandı!**\n\n" +
                            "🏆 **Sizin Smart Score: %d**\n" +
                            "━━━━━━━━━━━━━━━━━━\n" +
                            "📈 Gəlir Stabilliyi: %d%%\n" +
                            "📉 Xərc İdarəsi: %d%%\n" +
                            "⚖️ Balans Dinamikası: %d%%\n" +
                            "💳 Ödəniş Tarixçəsi: %d%%\n\n" +
                            "📅 *Məlumatlar %d aylıq dövr üçün hesablanıb.*",
                    scoreResult.getScore(),
                    scoreResult.getIncomeStability(),
                    scoreResult.getExpenseControl(),
                    scoreResult.getBalanceDynamics(),
                    scoreResult.getPaymentHistory(),
                    scoreResult.getPeriodMonths()
            );

            sendMsg(chatId, responseText);
            System.out.println("7. Готово!");
        }
    }
    private String cleanAndTrimText(String rawText) {
        if (rawText == null || rawText.isEmpty()) return "";

        return Arrays.stream(rawText.split("\n"))
                .map(line -> {
                    String trimmed = line.trim();
                    if (trimmed.isEmpty()) return "";

                    // ✅ режем, но НЕ добавляем "..."
                    if (trimmed.length() > 255) {
                        return trimmed.substring(0, 255);
                    }
                    return trimmed;
                })
                .filter(line -> !line.isEmpty())
                .collect(Collectors.joining("\n"));
    }

    private void sendMsg(Long chatId, String text) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId.toString());
        message.setText(text);
        message.setParseMode("Markdown");
        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

}