package az.edu.itbrains.SmartScore.enums;

public enum CategoryType {
    INCOME,              // любые входящие (пополнения/переводы/кэшбек/и т.д.)
    REGULAR_INCOME,      // ✅ зарплата/стипендия/регулярный доход (для стабильности)
    DAILY,
    ESSENTIAL,
    CREDIT,
    TRANSFER,
    OTHER
}
