package apartments.exception;

import apartments.dto.response.ApiResponse;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.security.access.AccessDeniedException;
import java.io.IOException;
import java.util.Objects;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleRuntimeException(RuntimeException ex){

        ApiResponse<Void> response = ApiResponse.<Void>builder()
                .code(ErrorCodes.UNCATEGORIZED_EXCEPTION.getCode())
                .message(ErrorCodes.UNCATEGORIZED_EXCEPTION.getMessage())
                .build();
        return new ResponseEntity<>(response, ErrorCodes.UNCATEGORIZED_EXCEPTION.getStatusCode());
    }

    @ExceptionHandler(AppException.class)
    public ResponseEntity<ApiResponse<Void>> handleAppException(AppException ex) {
        ErrorCodes errorCodes = ex.getErrorCodes();
        ApiResponse<Void> response = ApiResponse.<Void>builder()
                .code(errorCodes.getCode())
                .message(errorCodes.getMessage())
                .build();
        return new ResponseEntity<>(response, errorCodes.getStatusCode());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public  ResponseEntity<ApiResponse<Void>> handlingValidation(MethodArgumentNotValidException ex){
        String enumKey = Objects.requireNonNull(ex.getFieldError()).getDefaultMessage();
        ErrorCodes errorCodes = ErrorCodes.valueOf(enumKey);
        ApiResponse<Void> response = ApiResponse.<Void>builder()
                .code(errorCodes.getCode())
                .message(errorCodes.getMessage())
                .build();
        return new ResponseEntity<>(response, errorCodes.getStatusCode());
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleEntityNotFoundException(EntityNotFoundException ex) {
        ApiResponse<Void> response = ApiResponse.<Void>builder()
                .code(ErrorCodes.ENTITY_NOT_FOUND.getCode())
                .message(ex.getMessage())
                .build();
        return new ResponseEntity<>(response, ErrorCodes.ENTITY_NOT_FOUND.getStatusCode());
    }

    @ExceptionHandler(IOException.class)
    public ResponseEntity<ApiResponse<Void>> handleIOException(IOException ex) {
        ApiResponse<Void> response = ApiResponse.<Void>builder()
                .code(ErrorCodes.IO_EXCEPTION.getCode())
                .message(ex.getMessage())
                .build();
        return new ResponseEntity<>(response, ErrorCodes.IO_EXCEPTION.getStatusCode());
    }

    @ExceptionHandler(value = AccessDeniedException.class)
    ResponseEntity<ApiResponse<Void>> handlingAccessDeniedException(AccessDeniedException exception) {
        ErrorCodes errorCode = ErrorCodes.UNAUTHORIZED;

        return ResponseEntity.status(errorCode.getStatusCode())
                .body(ApiResponse.<Void>builder()
                        .code(errorCode.getCode())
                        .message(errorCode.getMessage())
                        .build());
    }
}
