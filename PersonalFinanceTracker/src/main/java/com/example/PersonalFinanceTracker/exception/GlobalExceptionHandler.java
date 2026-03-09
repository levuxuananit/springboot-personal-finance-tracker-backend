package com.example.PersonalFinanceTracker.exception;

import com.example.PersonalFinanceTracker.dto.response.ApiResponse;
import com.example.PersonalFinanceTracker.dto.response.ValidationError;
import com.example.PersonalFinanceTracker.dto.response.ValidationErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;

/**
 * Bộ xử lý lỗi tập trung - Kết hợp logic từ Main và Budget-list
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    // 1. Xử lý lỗi không tìm thấy tài nguyên (Từ Budget-list)
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiResponse<Object>> handleNotFound(ResourceNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ApiResponse<>(false, ex.getMessage(), null));
    }

    // 2. Xử lý các lỗi Custom ApiException (Từ HEAD)
    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ApiResponse<Object>> handleApiException(ApiException ex) {
        return ResponseEntity.status(ex.getStatus())
                .body(new ApiResponse<>(false, ex.getMessage(), null));
    }

    // 3. Xử lý lỗi Validation (Dùng cấu hình DTO chuyên nghiệp của Budget-list)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ValidationErrorResponse> handleValidation(MethodArgumentNotValidException ex) {
        List<ValidationError> errors = ex.getBindingResult().getFieldErrors().stream()
                .map(this::toValidationError)
                .toList();

        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY) // 422
                .body(new ValidationErrorResponse(false, "Validation failed", errors));
    }

    // 4. Xử lý lỗi Unauthorized (Nếu cần bắt thêm từ logic nghiệp vụ)
    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<ApiResponse<Object>> handleUnauthorized(UnauthorizedException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new ApiResponse<>(false, ex.getMessage(), null));
    }

    private ValidationError toValidationError(FieldError error) {
        return new ValidationError(error.getField(), error.getDefaultMessage());
    }
}