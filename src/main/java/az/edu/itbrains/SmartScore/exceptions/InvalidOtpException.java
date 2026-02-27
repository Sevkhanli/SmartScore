package az.edu.itbrains.SmartScore.exceptions;

public class InvalidOtpException extends RuntimeException {
    public InvalidOtpException(String message) {
        super(message);
    }
}