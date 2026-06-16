package org.example.model;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

public class Transaction implements Comparable<Transaction> {
    private static final AtomicInteger nextId = new AtomicInteger(1);
    private final int transactionId;
    private final String accountNumber;
    private final TransactionType type;
    private final double amount;
    private final double balanceAfter;
    private final LocalDateTime dateTime;

    public static final DateTimeFormatter DISPLAY_FORMAT =
            DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm:ss");
    //                                   20 Apr 2026, 14:30:45

    public static final DateTimeFormatter SHORT_FORMAT =
            DateTimeFormatter.ofPattern("dd/MM HH:mm");
    //                                   20/04 14:30

    public static final DateTimeFormatter FILE_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");


    public Transaction(String accountNumber, TransactionType type, double amount, double balanceAfter) {
        this.transactionId = nextId.getAndIncrement();
        this.accountNumber = accountNumber;
        this.type = type;
        this.amount = amount;
        this.balanceAfter = balanceAfter;
        this.dateTime = LocalDateTime.now();
    }

    public Transaction(String accountNumber, TransactionType type,
                       double amount, double balanceAfter, LocalDateTime dateTime) {
        this.transactionId = nextId.getAndIncrement();
        this.accountNumber = accountNumber;
        this.type = type;
        this.amount = amount;
        this.balanceAfter = balanceAfter;
        this.dateTime = dateTime;
    }

    @Override
    public int compareTo(Transaction other) {
        return Integer.compare(this.transactionId, other.transactionId);
    }

    public String ToDataString() {
        return String.join("|",
                String.valueOf(transactionId),
                type.name(),
                String.valueOf(amount),
                String.valueOf(balanceAfter)
        );
    }

    public Integer getId() {
        return transactionId;
    }

    public int compareAmountTo(Transaction other) {
        return Double.compare(amount, other.amount);
    }

    public boolean isAmountBetween(double min, double max) {
        if (Double.isNaN(min) || Double.isNaN(max)) {
            throw new IllegalArgumentException("Min and Max cannot be NaN");
        }
        return amount >= min && amount <= max;
    }

    // Format for display: "20 Apr 2026, 14:30:45"
    public String getFormattedDateTime() {
        return dateTime.format(DISPLAY_FORMAT);
    }

    // Format for receipts: "20/04 14:30"
    public String getShortDateTime() {
        return dateTime.format(SHORT_FORMAT);
    }

    // Format for file storage: "2026-04-20T14:30:45"
    public String getFileDateTime() {
        return dateTime.format(FILE_FORMAT);
    }

    // 🆕 How long ago was this transaction?
    public String getTimeAgo() {
        Duration elapsed = Duration.between(dateTime, LocalDateTime.now());

        long seconds = elapsed.getSeconds();
        if (seconds < 60) return seconds + "s ago";
        if (seconds < 3600) return (seconds / 60) + "m ago";
        if (seconds < 86400) return (seconds / 3600) + "h ago";
        return (seconds / 86400) + "d ago";
    }

    // 🆕 Was this transaction today?
    public boolean isToday() {
        return dateTime.toLocalDate().equals(java.time.LocalDate.now());
    }

    // 🆕 Was this transaction within the last N hours?
    public boolean isWithinLastHours(int hours) {
        LocalDateTime cutoff = LocalDateTime.now().minusHours(hours);
        return dateTime.isAfter(cutoff);
    }

    // 🆕 Was this transaction between two dates?
    public boolean isBetween(LocalDateTime start, LocalDateTime end) {
        return !dateTime.isBefore(start) && !dateTime.isAfter(end);
    }

    // 🆕 Updated to include datetime in file format
    public String toDataString() {
        return String.join("|",
                String.valueOf(transactionId),
                type.name(),
                String.valueOf(amount),
                String.valueOf(balanceAfter),
                getFileDateTime()  // 🆕 save as formatted string
        );
    }

    // 🆕 Updated to parse datetime from file
    public static Transaction fromString(String data, String accountNumber) {
        String[] parts = data.split("\\|");

        TransactionType parsedType = TransactionType.fromString(parts[1]);
        double parsedAmount = Double.parseDouble(parts[2]);
        double parsedBalance = Double.parseDouble(parts[3]);

        LocalDateTime parsedDateTime;
        if (parts.length >= 5 && !parts[4].isBlank()) {
            // Parse the saved datetime string back to LocalDateTime
            parsedDateTime = LocalDateTime.parse(parts[4], FILE_FORMAT);
        } else {
            parsedDateTime = LocalDateTime.now(); // fallback
        }

        return new Transaction(accountNumber, parsedType,
                parsedAmount, parsedBalance, parsedDateTime);
    }


    // --- Standard getters ---

    public String getAccountNumber() {
        return accountNumber;
    }

    public TransactionType getType() {
        return type;
    }

    public String getTypeName() {
        return type.displayName();
    }

    public double getAmount() {
        return amount;
    }

    public double getBalanceAfter() {
        return balanceAfter;
    }

    public LocalDateTime getDateTime() {
        return dateTime;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Transaction other)) return false;
        return this.transactionId == other.transactionId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(transactionId);
    }

    // 🆕 Updated toString with formatted datetime
    @Override
    public String toString() {
        return String.format("TXN-%04d | %-12s | %s | Balance: $%.2f | %s",
                transactionId,
                type.displayName(),
                type.formatAmount(amount),
                balanceAfter,
                getShortDateTime());  // 🆕 human-readable date
    }
}
