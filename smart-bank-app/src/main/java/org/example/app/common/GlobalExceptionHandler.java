
package org.example.app.common;

import org.example.app.common.dto.ErrorResponse;
import org.example.model.exception.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.security.core.AuthenticationException;

import java.time.Instant;
import java.util.stream.Collectors;

/**
 * Centralized exception handling for ALL controllers.
 * Maps each BankException → the correct HTTP status + clean message.
 * SRP: this class's ONE job is translating exceptions → HTTP responses.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    // --- 409 CONFLICT — duplicates (resource already exists) ---

    @ExceptionHandler(DuplicateEmailException.class)
    public ResponseEntity<ErrorResponse> handleDuplicateEmail(DuplicateEmailException e) {
        return build(HttpStatus.CONFLICT, e.getMessage());
    }

    @ExceptionHandler(DuplicateNameException.class)
    public ResponseEntity<ErrorResponse> handleDuplicateName(DuplicateNameException e) {
        return build(HttpStatus.CONFLICT, e.getMessage());
    }

    // --- 404 NOT FOUND — account doesn't exist ---

    @ExceptionHandler(AccountNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(AccountNotFoundException e) {
        return build(HttpStatus.NOT_FOUND, e.getMessage());
    }

    // --- 400 BAD REQUEST — invalid input ---

    @ExceptionHandler(InvalidAmountException.class)
    public ResponseEntity<ErrorResponse> handleInvalidAmount(InvalidAmountException e) {
        return build(HttpStatus.BAD_REQUEST, e.getMessage());
    }

    // --- 409 CONFLICT — business rule violations on current state ---

    @ExceptionHandler(InsufficientFundsException.class)
    public ResponseEntity<ErrorResponse> handleInsufficientFunds(InsufficientFundsException e) {
        return build(HttpStatus.CONFLICT, e.getMessage());
    }

    @ExceptionHandler(AccountFrozenException.class)
    public ResponseEntity<ErrorResponse> handleFrozen(AccountFrozenException e) {
        return build(HttpStatus.CONFLICT, e.getMessage());
    }

    // --- catch-all for any other BankException (defensive) ---

    @ExceptionHandler(BankException.class)
    public ResponseEntity<ErrorResponse> handleBankException(BankException e) {
        return build(HttpStatus.BAD_REQUEST, e.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(
            MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getFieldErrors().stream()
                .map(err -> err.getField() + ": " + err.getDefaultMessage())
                .collect(Collectors.joining(", "));

        return build(HttpStatus.BAD_REQUEST, message);
    }

    // --- helper: build a consistent error response ---

    private ResponseEntity<ErrorResponse> build(HttpStatus status, String message) {
        ErrorResponse body = new ErrorResponse(
                Instant.now(),
                status.value(),
                status.getReasonPhrase(),
                message
        );
        return ResponseEntity.status(status).body(body);
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorResponse> handleAuthentication(
            AuthenticationException exception) {

        return build(
                HttpStatus.UNAUTHORIZED,
                "Invalid username or password"
        );
    }
}