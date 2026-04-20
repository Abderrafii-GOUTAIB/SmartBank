package org.example.service;

import org.example.model.AccountStatus;
import org.example.model.BankAccount;
import org.example.model.Transaction;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class DataStore {
    private static final Path DATA_DIR = Path.of("smartbank-data");
    private static final Path ACCOUNTS_FILE = DATA_DIR.resolve("accounts.dat");
    private static final Path TRANSACTIONS_FILE = DATA_DIR.resolve("transactions.dat");
    private static final Path FLAGGED_FILE = DATA_DIR.resolve("flagged.dat");

    // Delimiter — the character that separates fields in each line
    private static final String DELIMITER = "|";

    public static void initialize() throws IOException {
        if (!Files.exists(DATA_DIR)) {
            Files.createDirectories(DATA_DIR);
            System.out.println("📁 Created data directory: " + DATA_DIR);
        }
    }

    public static void saveAccounts(List<BankAccount> accounts) throws IOException {
        initialize();
        StringBuilder builder = new StringBuilder();
        for (BankAccount account : accounts) {
            // Format: accountNumber|ownerName|balance|status|transactionCount
            builder.append(String.join(DELIMITER,
                    account.getAccountNumber(),
                    account.getOwnerName(),
                    account.getEmail(),
                    String.format("%.2f", account.getBalance()),
                    account.getStatus().name(),
                    String.valueOf(account.getTransactionCount())
            ));
            builder.append(System.lineSeparator());
        }
        Files.writeString(ACCOUNTS_FILE, builder.toString(),
                StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        System.out.println("💾 Saved " + accounts.size() + " account(s) to " + ACCOUNTS_FILE);
    }
    public static List<BankAccount> loadAccounts() throws IOException {
        List<BankAccount> accounts = new ArrayList<>();

        // Check if file exists first — first run won't have data
        if (!Files.exists(ACCOUNTS_FILE)) {
            System.out.println("ℹ️  No saved data found. Starting fresh.");
            return accounts; // empty list
        }

        // Files.readAllLines — reads EVERY line into a List<String>
        // For huge files use Files.lines() with streams (Phase 3)
        List<String> lines = Files.readAllLines(ACCOUNTS_FILE);

        for (String line : lines) {
            // Skip empty lines
            if (line.isBlank()) continue;

            try {
                // Split by delimiter — "ACC-0001|Alice|alice@mail.com|2500.75|ACTIVE"
                // becomes ["ACC-0001", "Alice", "alice@mail.com", "2500.75", "ACTIVE"]
                String[] parts = line.split("\\|"); // pipe needs escaping in regex

                if (parts.length < 5) {
                    System.out.println("⚠️  Skipping malformed line: " + line);
                    continue;
                }

                String accNum   = parts[0];
                String name     = parts[1];
                String email    = parts[2];
                double balance  = Double.parseDouble(parts[3]);
                String statusStr = parts[4];

                // Create account with loaded data
                BankAccount acc = new BankAccount(name, email, balance);

                // Restore status from saved enum name
                // AccountStatus.valueOf("FROZEN") → AccountStatus.FROZEN
                AccountStatus savedStatus = AccountStatus.valueOf(statusStr);
                if (savedStatus != AccountStatus.ACTIVE) {
                    // Account was created as ACTIVE, so only change if different
                    // We need to go through valid transitions
                    try {
                        if (savedStatus == AccountStatus.FROZEN) {
                            acc.freeze();
                        } else if (savedStatus == AccountStatus.CLOSED) {
                            acc.close();
                        }
                    } catch (IllegalStateException e) {
                        System.out.println("⚠️  Could not restore status for " + accNum);
                    }
                }

                accounts.add(acc);

            } catch (Exception e) {
                System.out.println("⚠️  Error parsing line: " + line + " — " + e.getMessage());
            }
        }

        System.out.println("📂 Loaded " + accounts.size() + " accounts from " + ACCOUNTS_FILE);
        return accounts;
    }
    public static void saveTransactions(List<BankAccount> accounts) throws IOException {
        initialize();

        StringBuilder sb = new StringBuilder();

        for (BankAccount acc : accounts) {
            for (Transaction txn : acc.getTransactions()) {
                sb.append(String.join(DELIMITER,
                        txn.getAccountNumber(),
                        txn.getType().name(),         // DEPOSIT, WITHDRAWAL, etc.
                        String.valueOf(txn.getAmount()),
                        String.valueOf(txn.getBalanceAfter()),
                        String.valueOf(txn.getTimestamp())
                ));
                sb.append(System.lineSeparator());
            }
        }

        Files.writeString(TRANSACTIONS_FILE, sb.toString(),
                StandardOpenOption.CREATE,
                StandardOpenOption.TRUNCATE_EXISTING);

        int totalTxns = accounts.stream()
                .mapToInt(BankAccount::getTransactionCount)
                .sum();
        System.out.println("💾 Saved " + totalTxns + " transactions to " + TRANSACTIONS_FILE);
    }

    public static void saveFlagged(Set<String> flaggedAccounts) throws IOException {
        initialize();
        // String.join with newline — each flagged account number on its own line
        String content = String.join(System.lineSeparator(), flaggedAccounts);

        Files.writeString(FLAGGED_FILE, content,
                StandardOpenOption.CREATE,
                StandardOpenOption.TRUNCATE_EXISTING);

        System.out.println("💾 Saved " + flaggedAccounts.size() + " flagged accounts");
    }
    public static Set<String> loadFlagged() throws IOException {
        Set<String> flagged = new LinkedHashSet<>();

        if (!Files.exists(FLAGGED_FILE)) {
            System.out.println("ℹ️  No flagged accounts found. Starting with empty set.");
            return flagged;
        }

        List<String> lines = Files.readAllLines(FLAGGED_FILE);
        for (String line : lines) {
            if (!line.isBlank()) {
                flagged.add(line.trim());
            }
        }

        System.out.println("📂 Loaded " + flagged.size() + " flagged accounts");
        return flagged;
    }

    public static void saveAll(AccountManager manager) {
        try {
            List<BankAccount> accounts = manager.getAllAccounts();
            saveAccounts(accounts);
            saveTransactions(accounts);

            // We need access to flagged accounts — let's add a getter
            // For now, save what we can
            System.out.println("✅ All data saved successfully.");

        } catch (IOException e) {
            System.out.println("❌ Error saving data: " + e.getMessage());
        }
    }

    public static void printFileInfo() {
        System.out.println();
        System.out.println("--- Data Files ---");

        printSingleFileInfo(ACCOUNTS_FILE);
        printSingleFileInfo(TRANSACTIONS_FILE);
        printSingleFileInfo(FLAGGED_FILE);
    }

    private static void printSingleFileInfo(Path path) {
        try {
            if (Files.exists(path)) {
                // Files.size() returns file size in bytes
                long size = Files.size(path);
                // Files.readAllLines().size() gives line count
                long lines = Files.readAllLines(path).size();
                System.out.printf("  %-25s %,d bytes  (%d records)%n",
                        path.getFileName(), size, lines);
            } else {
                System.out.println("  " + path.getFileName() + "  (not created yet)");
            }
        } catch (IOException e) {
            System.out.println("  " + path.getFileName() + "  (error reading)");
        }
    }

    public static void deleteAll() {
        try {
            if (Files.exists(ACCOUNTS_FILE))     Files.delete(ACCOUNTS_FILE);
            if (Files.exists(TRANSACTIONS_FILE)) Files.delete(TRANSACTIONS_FILE);
            if (Files.exists(FLAGGED_FILE))      Files.delete(FLAGGED_FILE);
            System.out.println("🗑️  All data files deleted.");
        } catch (IOException e) {
            System.out.println("❌ Error deleting files: " + e.getMessage());
        }
    }

    // ==================================================
    // APPEND — add a single line to a file (for logging)
    // ==================================================

    public static void appendLog(String message) {
        try {
            initialize();
            Path logFile = DATA_DIR.resolve("audit.log");

            // APPEND = add to the end (don't overwrite)
            // CREATE = create if it doesn't exist
            String logLine = System.currentTimeMillis() + DELIMITER + message
                    + System.lineSeparator();

            Files.writeString(logFile, logLine,
                    StandardOpenOption.CREATE,
                    StandardOpenOption.APPEND);

        } catch (IOException e) {
            // Logging should never crash the app — silently fail
            System.err.println("Warning: could not write to audit log");
        }
    }

}



