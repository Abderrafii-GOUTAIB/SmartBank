package org.example.model.exception;

public class StorageException extends BankException {

    public StorageException(String message, Throwable cause) {
        super(message, "STORAGE_ERROR", cause);
    }

    public StorageException(String message) {
        super(message, "STORAGE_ERROR");
    }
}