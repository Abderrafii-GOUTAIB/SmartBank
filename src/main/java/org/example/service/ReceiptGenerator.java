package org.example.service;

import org.example.model.BankAccount;
import org.example.model.Transaction;

import java.util.List;

public class ReceiptGenerator {

    private static final int RECEIPT_WIDTH = 50;
    private static final char HORIZONTAL = '═';
    private static final char VERTICAL   = '║';

    public static String generateTransactionReceipt(BankAccount account,
                                                    Transaction txn) {
        StringBuilder sb = new StringBuilder();

        sb.append(topBorder()).append('\n');
        sb.append(centerText("TRANSACTION RECEIPT")).append('\n');
        sb.append(centerText(BankAccount.BANK_NAME)).append('\n');
        sb.append(divider()).append('\n');

        sb.append(formatLine("Receipt #", String.format("TXN-%04d", txn.getId()))).append('\n');
        sb.append(formatLine("Account", account.getAccountNumber())).append('\n');
        sb.append(formatLine("Name", account.getOwnerName())).append('\n');
        sb.append(divider()).append('\n');

        // 🆕 ENUM FIX: use displayName() and formatAmount() from TransactionType
        sb.append(formatLine("Type", txn.getType().displayName())).append('\n');
        sb.append(formatLine("Amount", txn.getType().formatAmount(txn.getAmount()))).append('\n');

        String balanceStr = String.format("$%.2f", txn.getBalanceAfter());
        sb.append(formatLine("Balance After", balanceStr)).append('\n');
        sb.append(formatLine("Date", txn.getFormattedDateTime())).append('\n');
        sb.append(formatLine("", txn.getTimeAgo())).append('\n');

        sb.append(divider()).append('\n');
        sb.append(centerText("Thank you for banking with us!")).append('\n');

        sb.append(centerText("Thank you for banking with us!")).append('\n');
        sb.append(bottomBorder()).append('\n');

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

        // 🆕 ENUM FIX: use status icon and description
        sb.append(formatLine("Status",
                account.getStatus().icon() + " " + account.getStatus().description())).append('\n');
        sb.append(formatLine("Transactions",
                String.valueOf(account.getTransactionCount()))).append('\n');

        List<Transaction> transactions = account.getTransactions();

        if (!transactions.isEmpty()) {
            sb.append(divider()).append('\n');
            sb.append(centerText("TRANSACTION HISTORY")).append('\n');
            sb.append(divider()).append('\n');

            sb.append(VERTICAL).append(' ');
            sb.append(padRight("ID", 10));
            sb.append(padRight("TYPE", 14)); // 🆕 wider for display names
            sb.append(padRight("AMOUNT", 12));
            sb.append(padRight("BALANCE", 10));
            sb.append(padRight("TIME", 13));
            sb.append(VERTICAL).append('\n');
            sb.append(divider()).append('\n');

            for (Transaction txn : transactions) {
                sb.append(VERTICAL).append(' ');
                sb.append(padRight(String.format("TXN-%04d", txn.getId()), 10));
                // 🆕 ENUM FIX: use displayName() from TransactionType
                sb.append(padRight(txn.getType().displayName(), 14));
                // 🆕 ENUM FIX: use formatAmount() for +/- prefix
                sb.append(padRight(txn.getType().formatAmount(txn.getAmount()), 12));
                sb.append(padRight(String.format("$%.2f", txn.getBalanceAfter()), 10));
                sb.append(padRight(txn.getShortDateTime(), 13));
                sb.append(VERTICAL).append('\n');
            }
        }

        sb.append(bottomBorder()).append('\n');

        return sb.toString();
    }

    public static String generateBankSummary(BankAccount... accounts) {
        StringBuilder sb = new StringBuilder();

        sb.append(topBorder()).append('\n');
        sb.append(centerText(BankAccount.BANK_NAME + " — BANK SUMMARY")).append('\n');
        sb.append(divider()).append('\n');

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

            String name = account.getOwnerName();
            if (name.length() > 15) {
                name = name.substring(0, 12) + "...";
            }
            sb.append(padRight(name, 16));
            sb.append(padRight(String.format("$%.2f", account.getBalance()), 12));
            sb.append("  ").append(VERTICAL).append('\n');

            totalBalance += account.getBalance();
            validCount++;
        }

        sb.append(divider()).append('\n');
        sb.append(formatLine(
                validCount + " accounts",
                String.format("$%.2f total", totalBalance)
        )).append('\n');
        sb.append(bottomBorder()).append('\n');

        return sb.toString();
    }

    public static String highlightMatch(String text, String searchTerm) {
        String lowerText = text.toLowerCase();
        String lowerSearch = searchTerm.toLowerCase();
        if (!lowerText.contains(lowerSearch)) return text;
        int index = lowerText.indexOf(lowerSearch);
        return text.substring(0, index) +
                ">>>" +
                text.substring(index, index + searchTerm.length()) +
                "<<<" +
                text.substring(index + searchTerm.length());
    }

    private static String padRight(String text, int width) {
        if (text.length() >= width) return text.substring(0, width);
        return String.format("%-" + width + "s", text);
    }

    private static String centerText(String text) {
        if (text.length() >= RECEIPT_WIDTH - 4) {
            return VERTICAL + " " + text + " " + VERTICAL;
        }
        int totalPadding = RECEIPT_WIDTH - 2 - text.length();
        int leftPad = totalPadding / 2;
        int rightPad = totalPadding - leftPad;
        return VERTICAL + " ".repeat(leftPad) + text + " ".repeat(rightPad) + VERTICAL;
    }

    private static String formatLine(String key, String value) {
        int contentWidth = RECEIPT_WIDTH - 4;
        int valueWidth = contentWidth - key.length() - 3;
        return String.format("%c %-" + (contentWidth - valueWidth - 1) + "s %" + valueWidth + "s %c",
                VERTICAL, key + ":", value, VERTICAL);
    }

    private static String topBorder() {
        return "╔" + String.valueOf(HORIZONTAL).repeat(RECEIPT_WIDTH - 2) + "╗";
    }

    private static String bottomBorder() {
        return "╚" + String.valueOf(HORIZONTAL).repeat(RECEIPT_WIDTH - 2) + "╝";
    }

    private static String divider() {
        return "╠" + String.valueOf(HORIZONTAL).repeat(RECEIPT_WIDTH - 2) + "╣";
    }
}