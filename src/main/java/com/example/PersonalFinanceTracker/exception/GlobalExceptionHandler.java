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

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiResponse<Object>> handleNotFound(ResourceNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ApiResponse<>(false, ex.getMessage(), null));
    }

    @ExceptionHandler(UnprocessableEntityException.class)
    public ResponseEntity<ValidationErrorResponse> handleUnprocessable(UnprocessableEntityException ex) {
        return ResponseEntity.status(422)
                .body(new ValidationErrorResponse(
                        false,
                        "Validation failed",
                        List.of(new ValidationError(ex.getField(), ex.getMessage()))
                ));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ValidationErrorResponse> handleValidation(MethodArgumentNotValidException ex) {
        List<ValidationError> errors = ex.getBindingResult().getFieldErrors().stream()
                .map(this::toValidationError)
                .toList();

        return ResponseEntity.status(422)
                .body(new ValidationErrorResponse(false, "Validation failed", errors));
    }

    private ValidationError toValidationError(FieldError error) {
        return new ValidationError(error.getField(), error.getDefaultMessage());
    }
}

