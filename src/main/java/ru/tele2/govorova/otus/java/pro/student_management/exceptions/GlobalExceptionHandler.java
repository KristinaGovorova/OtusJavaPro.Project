package ru.tele2.govorova.otus.java.pro.student_management.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.tele2.govorova.otus.java.pro.student_management.dto.response.ApiErrorResponse;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(StudentValidationException.class)
    public ResponseEntity<ApiErrorResponse> handleValidationException(StudentValidationException e) {
        String message = "Ошибки валидации: " + String.join("; ", e.getErrors());
        return ResponseEntity
                .badRequest()
                .body(new ApiErrorResponse(message, 400));
    }

    @ExceptionHandler(VersionConflictException.class)
    public ResponseEntity<ApiErrorResponse> handleVersionConflictException(VersionConflictException e) {
        ApiErrorResponse errorResponse = new ApiErrorResponse(e.getMessage(), 409);
        return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse);
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ApiErrorResponse> handleRuntimeException(RuntimeException e) {
        ApiErrorResponse errorResponse = new ApiErrorResponse(e.getMessage(), 500);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }
}
