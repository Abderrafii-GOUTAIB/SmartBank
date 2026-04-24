package org.example.model.exception;

public class DuplicateEmailException extends AccountException {

    private final String email;

    public DuplicateEmailException(String email) {
        super("Email already exists: " + email,
                "DUPLICATE_EMAIL", null);
        this.email = email;
    }

    public String getEmail() {
        return email;
    }

}
