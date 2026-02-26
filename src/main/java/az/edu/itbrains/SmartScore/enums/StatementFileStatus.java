package az.edu.itbrains.SmartScore.enums;

public enum StatementFileStatus {
    UPLOADED,   // Файл просто упал в базу
    PROCESSING, // Идет парсинг и работа с GPT (Экран 2 - Analiz)
    COMPLETED,  // Всё готово, баллы посчитаны (Экран 3 - Nəticə)
    FAILED      // Что-то пошло не так
}
