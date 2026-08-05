package org.example.service;

import org.example.model.BankEvent;

public class AuditObserver implements BankEventObserver {

    private final StoragePort storage;

    public AuditObserver(StoragePort storage) {
        this.storage = storage;
    }

    @Override
    public void onEvent(BankEvent event) {
        storage.logAudit(event.getEventType() + ": " + event.getDescription());
    }
}
