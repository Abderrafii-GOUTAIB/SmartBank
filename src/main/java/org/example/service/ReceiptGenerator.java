package org.example.service;


import org.example.model.BankAccount;
import org.example.model.Transaction;

import java.util.List;

public class ReceiptGenerator {

    // Box-drawing characters for professional receipts
    private static final int RECEIPT_WIDTH = 50;
    private static final char HORIZONTAL = '═';
    private static final char VERTICAL   = '║';

    // ================================================
    // STRING BUILDER — building complex strings efficiently
    // ================================================

    public static String generateTransactionReceipt(BankAccount account,
                                                    Transaction txn) {
        // StringBuilder — one mutable buffer, no wasted copies
        StringBuilder sb = new StringBuilder();

        sb.append(topBorder()).append('\n');
        sb.append(centerText("TRANSACTION RECEIPT")).append('\n');
        sb.append(centerText(BankAccount.BANK_NAME)).append('\n');
        sb.append(divider()).append('\n');

        // String.format() — precise column alignment
        sb.append(formatLine("Receipt #", String.format("TXN-%04d", txn.getId()))).append('\n');
        sb.append(formatLine("Account", account.getAccountNumber())).append('\n');
        sb.append(formatLine("Name", account.getOwnerName())).append('\n');
        sb.append(divider()).append('\n');

        sb.append(formatLine("Type", txn.getType())).append('\n');

        // ================================================
        // STRING METHODS — manipulation and formatting
        // ================================================

        // String.format() with format specifiers:
        //   %s   = string
        //   %d   = integer
        //   %f   = floating point
        //   %.2f = floating point with 2 decimal places
        //   %10.2f = right-aligned in 10-char field, 2 decimals
        //   %-15s = left-aligned in 15-char field
        String amountStr = String.format("$%.2f", txn.getAmount());
        String balanceStr = String.format("$%.2f", txn.getBalanceAfter());

        sb.append(formatLine("Amount", amountStr)).append('\n');
        sb.append(formatLine("Balance After", balanceStr)).append('\n');
        sb.append(divider()).append('\n');

        // Generate a simple timestamp string
        sb.append(centerText("Thank you for banking with us!")).append('\n');
        sb.append(bottomBorder()).append('\n');

        // .toString() converts StringBuilder → String (the final product)
        return sb.toString();
    }

    public static String generateAccountReport(BankAccount account) {
        StringBuilder sb = new StringBuilder();

        sb.append(topBorder()).append('\n');
        sb.append(centerText("ACCOUNT REPORT")).append('\n');
        sb.append(centerText(BankAccount.BANK_NAME)).append('\n');
        sb.append(divider()).append('\n');

        sb.append(formatLine("Account #", account.getAccountNumber())).append('\n');
        sb.append(formatLine("Owner", account.getOwnerName())).append('\n');
        sb.append(formatLine("Balance", String.format("$%.2f", account.getBalance()))).append('\n');
        sb.append(formatLine("Status", account.isActive() ? "Active" : "Frozen")).append('\n');
        sb.append(formatLine("Transactions", String.valueOf(account.getTransactionCount()))).append('\n');

        // ================================================
        // TRANSACTION HISTORY with String manipulation
        // ================================================

        List<Transaction> transactions = account.getTransactions();

        if (!transactions.isEmpty()) {
            sb.append(divider()).append('\n');
            sb.append(centerText("TRANSACTION HISTORY")).append('\n');
            sb.append(divider()).append('\n');

            // Column headers
            sb.append(VERTICAL).append(' ');
            sb.append(padRight("ID", 10));
            sb.append(padRight("TYPE", 12));
            sb.append(padRight("AMOUNT", 12));
            sb.append(padRight("BALANCE", 12));
            sb.append(VERTICAL).append('\n');
            sb.append(divider()).append('\n');

            for (Transaction txn : transactions) {
                sb.append(VERTICAL).append(' ');

                // padRight() — left-align text in a fixed-width column
                sb.append(padRight(String.format("TXN-%04d", txn.getId()), 10));
                sb.append(padRight(txn.getType(), 12));
                sb.append(padRight(String.format("$%.2f", txn.getAmount()), 12));
                sb.append(padRight(String.format("$%.2f", txn.getBalanceAfter()), 12));
                sb.append(VERTICAL).append('\n');
            }
        }

        sb.append(bottomBorder()).append('\n');

        return sb.toString();
    }

    // ================================================
    // SUMMARY REPORT — replaces the old printSummary()
    // ================================================

    public static String generateBankSummary(BankAccount... accounts) {
        StringBuilder sb = new StringBuilder();

        sb.append(topBorder()).append('\n');
        sb.append(centerText(BankAccount.BANK_NAME + " — BANK SUMMARY")).append('\n');
        sb.append(divider()).append('\n');

        // Column headers
        sb.append(VERTICAL).append(' ');
        sb.append(padRight("ACCOUNT", 12));
        sb.append(padRight("OWNER", 16));
        sb.append(padRight("BALANCE", 12));
        sb.append("  ").append(VERTICAL).append('\n');
        sb.append(divider()).append('\n');

        double totalBalance = 0;
        int validCount = 0;

        for (BankAccount account : accounts) {
            if (account == null) continue;

            sb.append(VERTICAL).append(' ');
            sb.append(padRight(account.getAccountNumber(), 12));

            // ================================================
            // STRING METHODS SHOWCASE
            // ================================================

            // .substring() — get first 15 chars of name (truncate long names)
            String name = account.getOwnerName();
            if (name.length() > 15) {
                name = name.substring(0, 12) + "...";
                // substring(start, end) — returns chars from start to end-1
            }
            sb.append(padRight(name, 16));

            sb.append(padRight(String.format("$%.2f", account.getBalance()), 12));
            sb.append("  ").append(VERTICAL).append('\n');

            totalBalance += account.getBalance();
            validCount++;
        }

        sb.append(divider()).append('\n');

        // Total line
        sb.append(formatLine(
                validCount + " accounts",
                String.format("$%.2f total", totalBalance)
        )).append('\n');

        sb.append(bottomBorder()).append('\n');

        return sb.toString();
    }

    // ================================================
    // SEARCH RESULT FORMATTING
    // ================================================

    public static String highlightMatch(String text, String searchTerm) {
        // Case-insensitive search using toLowerCase()
        // .toLowerCase() returns a NEW string (strings are immutable!)
        String lowerText = text.toLowerCase();
        String lowerSearch = searchTerm.toLowerCase();

        // .contains() — check if one string exists inside another
        if (!lowerText.contains(lowerSearch)) {
            return text; // no match, return original
        }

        // .indexOf() — find WHERE the match starts
        int index = lowerText.indexOf(lowerSearch);

        // .substring() — extract parts
        // Build: "before" + ">>>MATCH<<<" + "after"
        StringBuilder result = new StringBuilder();
        result.append(text.substring(0, index));             // before match
        result.append(">>>");
        result.append(text.substring(index, index + searchTerm.length())); // the match
        result.append("<<<");
        result.append(text.substring(index + searchTerm.length()));        // after match

        return result.toString();
    }

    // ================================================
    // HELPER METHODS — String manipulation building blocks
    // ================================================

    // Pad a string to a fixed width (left-aligned)
    private static String padRight(String text, int width) {
        if (text.length() >= width) {
            return text.substring(0, width);
        }
        // %-Xs = left-aligned in X-char field
        return String.format("%-" + width + "s", text);
    }

    // Center text within the receipt width
    private static String centerText(String text) {
        if (text.length() >= RECEIPT_WIDTH - 4) {
            return VERTICAL + " " + text + " " + VERTICAL;
        }

        int totalPadding = RECEIPT_WIDTH - 2 - text.length(); // -2 for borders
        int leftPad = totalPadding / 2;
        int rightPad = totalPadding - leftPad;

        // " ".repeat(n) — creates a string of n spaces (Java 11+)
        // This is much cleaner than a loop!
        return VERTICAL
                + " ".repeat(leftPad)
                + text
                + " ".repeat(rightPad)
                + VERTICAL;
    }

    // Format a key-value line
    private static String formatLine(String key, String value) {
        int contentWidth = RECEIPT_WIDTH - 4; // minus borders and spaces
        int valueWidth = contentWidth - key.length() - 3; // -3 for ": " and space

        // .repeat() — repeat a character n times
        return String.format("%c %-" + (contentWidth - valueWidth - 1) + "s %" + valueWidth + "s %c",
                VERTICAL, key + ":", value, VERTICAL);
    }

    // Border builders using String.repeat() and char
    private static String topBorder() {
        // ═ repeated N times
        return "╔" + String.valueOf(HORIZONTAL).repeat(RECEIPT_WIDTH - 2) + "╗";
    }

    private static String bottomBorder() {
        return "╚" + String.valueOf(HORIZONTAL).repeat(RECEIPT_WIDTH - 2) + "╝";
    }

    private static String divider() {
        return "╠" + String.valueOf(HORIZONTAL).repeat(RECEIPT_WIDTH - 2) + "╣";
    }
}