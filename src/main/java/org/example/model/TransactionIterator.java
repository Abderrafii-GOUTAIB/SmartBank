package org.example.model;

import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

// Implements Iterator<Transaction> — this makes it usable in while loops
public class TransactionIterator implements Iterator<Transaction> {

    private final List<Transaction> transactions;
    private final String filterType;  // null = all, "DEPOSIT" = only deposits, etc.
    private int cursor;               // the bookmark

    // Constructor for all transactions
    public TransactionIterator(List<Transaction> transactions) {
        this(transactions, null);
    }

    // Constructor with type filter
    public TransactionIterator(List<Transaction> transactions, String filterType) {
        // Take an immutable copy — protects against external modification
        this.transactions = List.copyOf(transactions);
        this.filterType = filterType;
        this.cursor = 0;
        advanceToNext(); // move cursor to first matching element
    }

    // Skip non-matching elements
    private void advanceToNext() {
        while (cursor < transactions.size() && !matches(transactions.get(cursor))) {
            cursor++;
        }
    }

    // Check if a transaction matches our filter
    private boolean matches(Transaction txn) {
        if (filterType == null) return true; // no filter = everything matches
        return txn.getType().equalsIgnoreCase(filterType);
    }

    @Override
    public boolean hasNext() {
        return cursor < transactions.size();
    }

    @Override
    public Transaction next() {
        if (!hasNext()) {
            throw new NoSuchElementException("No more transactions.");
        }
        Transaction txn = transactions.get(cursor);
        cursor++;
        advanceToNext(); // move to next matching element
        return txn;
    }

    // We don't support remove — this is a read-only iterator
    @Override
    public void remove() {
        throw new UnsupportedOperationException(
                "Cannot remove transactions through this iterator.");
    }
}