package org.example.service;

import org.example.model.BankEvent;


@FunctionalInterface
public interface BankEventObserver {
    void onEvent(BankEvent event);
}

