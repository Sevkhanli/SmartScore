package az.edu.itbrains.SmartScore.exceptions;

public class OtpExpiredException extends RuntimeException {
    public OtpExpiredException(String message) {
        super(message);
    }
}