package org.example.service;

import org.example.model.BankEvent;
import org.example.model.TransactionType;

/**
 * Observes TransactionEvents and records daily activity.
 * Replaces tracker.recordDeposit() and tracker.recordCategory()
 * calls scattered in AccountService.
 */
public class TransactionTrackerObserver implements BankEventObserver {

    private final TransactionTracker tracker;

    public TransactionTrackerObserver(TransactionTracker tracker) {
        this.tracker = tracker;
    }

    @Override
    public void onEvent(BankEvent event) {
        if (!(event instanceof BankEvent.TransactionEvent txnEvent)) return;

        // record category for every transaction type
        tracker.recordCategory(txnEvent.getType());

        // record deposit total only for credits coming IN
        if (txnEvent.getType().isCredit()) {
            tracker.recordDeposit(txnEvent.getAccountNumber(), txnEvent.getAmount());
        }
    }
}