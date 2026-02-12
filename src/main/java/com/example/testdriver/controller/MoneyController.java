package com.example.testdriver.controller;

import com.example.testdriver.domain.Money;
import com.example.testdriver.service.MoneyService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/money")
public class MoneyController {

    private final MoneyService moneyService;

    public MoneyController(MoneyService moneyService) {
        this.moneyService = moneyService;
    }

    @PostMapping("/growth")
    public ResponseEntity<MoneyResponse> applyGrowth(@Valid @RequestBody GrowthRequest request) {
        Money base = new Money(request.amount(), request.currency());
        Money result = moneyService.applyGrowth(base, request.ratePercent());
        return ResponseEntity.ok(new MoneyResponse(result.amount(), result.currency()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ValidationErrorResponse> handleValidation(MethodArgumentNotValidException ex) {
        List<FieldValidationError> errors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(this::toValidationError)
                .toList();
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ValidationErrorResponse("Validation failed", errors));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiErrorResponse> handleIllegalArgument(IllegalArgumentException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ApiErrorResponse(ex.getMessage()));
    }

    private FieldValidationError toValidationError(FieldError error) {
        String message = error.getDefaultMessage() == null ? "Invalid value" : error.getDefaultMessage();
        return new FieldValidationError(error.getField(), message);
    }

    public record GrowthRequest(
            @NotNull(message = "amount is required")
            @DecimalMin(value = "0.0", inclusive = true, message = "amount must be greater than or equal to 0")
            BigDecimal amount,

            @NotBlank(message = "currency is required")
            String currency,

            @NotNull(message = "ratePercent is required")
            @DecimalMin(value = "0.0", inclusive = true, message = "ratePercent must be greater than or equal to 0")
            @DecimalMax(value = "1000.0", inclusive = true, message = "ratePercent must be less than or equal to 1000")
            BigDecimal ratePercent
    ) {
    }

    public record MoneyResponse(BigDecimal amount, String currency) {
    }

    public record ApiErrorResponse(String message) {
    }

    public record ValidationErrorResponse(String message, List<FieldValidationError> errors) {
    }

    public record FieldValidationError(String field, String error) {
    }
}
