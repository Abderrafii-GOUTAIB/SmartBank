package org.example.model.exception;

public class BankException extends RuntimeException {

    private final String errorCode;

    public BankException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
    }

    public BankException(String message, String errorCode, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }

    public String getErrorCode() { return errorCode; }

    // Formatted error for display
    public String toDisplayString() {
        return String.format("[%s] %s", errorCode, getMessage());
    }
}