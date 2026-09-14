package org.example.model.exception;

public class DuplicateNameException extends AccountException {
    public final String name;

    public DuplicateNameException(String name) {
        super("Duplicate account name: " + name,"DUPLICATE_NAME", null);
        this.name = name;
    }

    public String getName() {
        return name;
    }
}

