package az.edu.itbrains.SmartScore.exceptions;

// RuntimeException-dan extend etdiyinize əmin olun
public class UserNotVerifiedException extends RuntimeException {

    private final String verificationToken;

    // 1. Sadə mesaj üçün konstruktor (Ənənəvi istifadə)
    public UserNotVerifiedException(String message) {
        super(message);
        this.verificationToken = null; // Token yoxdursa null qalsın
    }

    // 2. Mesaj və Token üçün konstruktor (YENİ - Sizin ehtiyacınız olan)
    public UserNotVerifiedException(String message, String verificationToken) {
        super(message);
        this.verificationToken = verificationToken;
    }

    // Tokeni Controller-də və ya Global Exception Handler-də əldə etmək üçün getter
    public String getVerificationToken() {
        return verificationToken;
    }
}
