package org.example.service;

import org.example.model.BankEvent;
import org.example.model.TransactionType;

/**
 * Observes TransactionEvents only.
 * Replaces riskService.checkTransaction() calls in AccountService.
 */
public class RiskObserver implements BankEventObserver {

    private final StoragePort storage;
    private static final double LARGE_THRESHOLD =
            BankConfig.getInstance().getLargeTransactionThreshold();

    public RiskObserver(StoragePort storage) {
        this.storage = storage;
    }

    @Override
    public void onEvent(BankEvent event) {
        // pattern matching on sealed class — Step 30
        // only react to TransactionEvents, ignore everything else
        if (!(event instanceof BankEvent.TransactionEvent txnEvent)) return;

        // only react to credits and debits — not system events
        if (txnEvent.getType() == TransactionType.INTEREST
                || txnEvent.getType() == TransactionType.FEE) return;

        if (txnEvent.getAmount() >= LARGE_THRESHOLD) {
            System.out.println("  🔔 ALERT: Large "
                    + txnEvent.getType().displayName()
                    + " of $" + String.format("%,.2f", txnEvent.getAmount())
                    + " on account " + txnEvent.getAccountNumber());

            storage.logAudit("LARGE_TXN: "
                    + txnEvent.getType().displayName()
                    + " $" + txnEvent.getAmount()
                    + " on " + txnEvent.getAccountNumber());
        }
    }
}