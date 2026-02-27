package az.edu.itbrains.SmartScore.handlers;

import az.edu.itbrains.SmartScore.dtos.response.AuthResponseDTO;
import az.edu.itbrains.SmartScore.exceptions.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.stream.Collectors;

@ControllerAdvice
public class GlobalExceptionHandler {

    // === ƏLAVƏ EDİLDİ: Validasiya (Regex, Size) xətalarını tutmaq üçün ===
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<AuthResponseDTO> handleValidationExceptions(MethodArgumentNotValidException ex) {
        // Bütün xətaları bir mətn halına gətiririk
        String errorMessage = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> error.getDefaultMessage())
                .collect(Collectors.joining(", "));

        return new ResponseEntity<>(
                new AuthResponseDTO(false, errorMessage),
                HttpStatus.BAD_REQUEST // 400
        );
    }

    // 404 - Tapılmadı (User, Telefon və ya Email)
    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<AuthResponseDTO> handleUserNotFound(UserNotFoundException ex) {
        return new ResponseEntity<>(
                new AuthResponseDTO(false, ex.getMessage()),
                HttpStatus.NOT_FOUND // 404
        );
    }

    // 409 - Konflikt (Təkrar Məlumat: Register zamanı)
    @ExceptionHandler(UserAlreadyExistsException.class)
    public ResponseEntity<AuthResponseDTO> handleUserAlreadyExists(UserAlreadyExistsException ex) {
        return new ResponseEntity<>(
                new AuthResponseDTO(false, ex.getMessage()),
                HttpStatus.CONFLICT // 409
        );
    }

    // 403 - Girişə İcazə Yoxdur (Təsdiqlənməyib)
    @ExceptionHandler(UserNotVerifiedException.class)
    public ResponseEntity<AuthResponseDTO> handleUserNotVerified(UserNotVerifiedException ex) {
        return new ResponseEntity<>(
                new AuthResponseDTO(false, ex.getMessage()),
                HttpStatus.FORBIDDEN // 403
        );
    }

    // 400 - Səhv Sorğu (OTP və ya etibarsız məlumat)
    @ExceptionHandler({
            InvalidOtpException.class,
            OtpExpiredException.class,
            UserAlreadyVerifiedException.class,
            InvalidCredentialsException.class // Login zamanı şifrə səhvi
    })
    public ResponseEntity<AuthResponseDTO> handleBadRequestExceptions(RuntimeException ex) {
        return new ResponseEntity<>(
                new AuthResponseDTO(false, ex.getMessage()),
                HttpStatus.BAD_REQUEST // 400
        );
    }

    // Hər hansı digər gözlənilməz xəta üçün (Opsional)
    @ExceptionHandler(Exception.class)
    public ResponseEntity<AuthResponseDTO> handleGeneralException(Exception ex) {
        return new ResponseEntity<>(
                new AuthResponseDTO(false, "Sistem xətası baş verdi: " + ex.getMessage()),
                HttpStatus.INTERNAL_SERVER_ERROR // 500
        );
    }
}