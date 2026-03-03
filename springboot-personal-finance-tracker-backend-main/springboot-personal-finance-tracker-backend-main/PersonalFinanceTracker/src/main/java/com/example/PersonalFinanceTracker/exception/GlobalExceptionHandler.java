package com.example.PersonalFinanceTracker.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * Bắt toàn bộ exception và format JSON theo spec
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Validation nhiều lỗi
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Object> handleValidation(MethodArgumentNotValidException ex) {

        List<Map<String, String>> errors = new ArrayList<>();

        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            Map<String, String> err = new HashMap<>();
            err.put("field", error.getField());
            err.put("message", error.getDefaultMessage());
            errors.add(err);
        }

        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("message", "Validation failed");
        response.put("errors", errors);

        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(response);
    }

    /**
     * Custom exception (409, 422, ...)
     */
    @ExceptionHandler(ApiException.class)
    public ResponseEntity<Object> handleApiException(ApiException ex) {

        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("message", ex.getMessage());

        return ResponseEntity.status(ex.getStatus()).body(response);
    }
    /**
     * Xử lý sai mật khẩu / chưa đăng nhập
     * -> HTTP 401
     */
    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<?> handleUnauthorized(UnauthorizedException ex) {
        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(Map.of(
                        "success", false,
                        "message", ex.getMessage()
                ));
    }

    /**
     * Xử lý lỗi request (email tồn tại, validate...)
     * -> HTTP 400
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleBadRequest(Exception ex) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(Map.of(
                        "success", false,
                        "message", ex.getMessage()
                ));
    }
}