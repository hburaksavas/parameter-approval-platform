package com.example.parameterapproval.common;

import jakarta.validation.ConstraintViolationException;
import com.example.parameterapproval.parameter.ChangeConflictException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class RestExceptionHandler {

    @ExceptionHandler(ChangeConflictException.class)
    ResponseEntity<ApiError> conflict(ChangeConflictException ex) {
        return response(HttpStatus.CONFLICT, "CHANGE_CONFLICT", ex.getMessage(), Map.of());
    }

    @ExceptionHandler(NotFoundException.class)
    ResponseEntity<ApiError> notFound(NotFoundException ex) {
        return response(HttpStatus.NOT_FOUND, "NOT_FOUND", ex.getMessage(), Map.of());
    }

    @ExceptionHandler({BusinessException.class, IllegalArgumentException.class, ConstraintViolationException.class})
    ResponseEntity<ApiError> badRequest(RuntimeException ex) {
        return response(HttpStatus.BAD_REQUEST, "BUSINESS_ERROR", ex.getMessage(), Map.of());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<ApiError> validation(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new LinkedHashMap<>();
        ex.getBindingResult().getFieldErrors()
                .forEach(error -> errors.putIfAbsent(error.getField(), error.getDefaultMessage()));
        return response(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", "İstek doğrulanamadı", errors);
    }

    private ResponseEntity<ApiError> response(
            HttpStatus status, String code, String message, Map<String, String> errors) {
        return ResponseEntity.status(status)
                .body(new ApiError(Instant.now(), status.value(), code, message, errors));
    }
}
