package org.example.service;

import org.example.model.AccountStatus;
import org.example.model.BankAccount;
import org.example.model.Transaction;
import org.example.model.exception.StorageException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class DataStore implements StoragePort {

    private static final Path DATA_DIR          = Path.of("smartbank-data");
    private static final Path ACCOUNTS_FILE     = DATA_DIR.resolve("accounts.dat");
    private static final Path TRANSACTIONS_FILE = DATA_DIR.resolve("transactions.dat");
    private static final Path FLAGGED_FILE      = DATA_DIR.resolve("flagged.dat");
    private static final String DELIMITER       = "|";

    // ── INITIALIZATION ────────────────────────────────────────────────────────

    public static void initialize() throws IOException {
        if (!Files.exists(DATA_DIR)) {
            Files.createDirectories(DATA_DIR);
            System.out.println("📁 Created data directory: " + DATA_DIR);
        }
    }

    // ── STATIC SAVE METHODS ───────────────────────────────────────────────────

    public static void saveAccounts(List<BankAccount> accounts) throws IOException {
        initialize();
        String content = accounts.stream()
                .map(acc -> String.join(DELIMITER,
                        acc.getAccountNumber(),
                        acc.getOwnerName(),
                        acc.getEmail(),
                        String.format("%.2f", acc.getBalance()),
                        acc.getStatus().name(),
                        acc.getCreatedAt().format(Transaction.FILE_FORMAT),
                        String.valueOf(acc.getTransactionCount())
                ))
                .collect(Collectors.joining(System.lineSeparator()));

        Files.writeString(ACCOUNTS_FILE, content + System.lineSeparator(),
                StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        System.out.println("💾 Saved " + accounts.size() + " account(s) to " + ACCOUNTS_FILE);
    }

    public static void saveTransactions(List<BankAccount> accounts) throws IOException {
        initialize();
        // flatMap — flatten all accounts' transactions into one stream (Phase 3)
        String content = accounts.stream()
                .flatMap(acc -> acc.getTransactions().stream())
                .map(txn -> String.join(DELIMITER,
                        txn.getAccountNumber(),
                        txn.getType().name(),
                        String.valueOf(txn.getAmount()),
                        String.valueOf(txn.getBalanceAfter()),
                        txn.getFileDateTime()
                ))
                .collect(Collectors.joining(System.lineSeparator()));

        Files.writeString(TRANSACTIONS_FILE, content + System.lineSeparator(),
                StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);

        int totalTxns = accounts.stream().mapToInt(BankAccount::getTransactionCount).sum();
        System.out.println("💾 Saved " + totalTxns + " transactions to " + TRANSACTIONS_FILE);
    }

    public static void saveFlagged(Set<String> flaggedAccounts) throws IOException {
        initialize();
        String content = String.join(System.lineSeparator(), flaggedAccounts);
        Files.writeString(FLAGGED_FILE, content,
                StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        System.out.println("💾 Saved " + flaggedAccounts.size() + " flagged accounts");
    }

    // ── STATIC UTILITY METHODS ────────────────────────────────────────────────

    public static void appendLog(String message) {
        // logAudit must never crash the app — silently fail on error
        try {
            initialize();
            Path logFile = DATA_DIR.resolve("audit.log");
            String logLine = System.currentTimeMillis() + DELIMITER + message
                    + System.lineSeparator();
            Files.writeString(logFile, logLine,
                    StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        } catch (IOException e) {
            System.err.println("Warning: could not write to audit log");
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
                long size  = Files.size(path);
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
            if (Files.exists(ACCOUNTS_FILE))      Files.delete(ACCOUNTS_FILE);
            if (Files.exists(TRANSACTIONS_FILE))   Files.delete(TRANSACTIONS_FILE);
            if (Files.exists(FLAGGED_FILE))        Files.delete(FLAGGED_FILE);
            System.out.println("🗑️  All data files deleted.");
        } catch (IOException e) {
            System.out.println("❌ Error deleting files: " + e.getMessage());
        }
    }

    // ── StoragePort IMPLEMENTATION ────────────────────────────────────────────

    @Override
    public void saveState(List<BankAccount> accounts) {
        try {
            saveAccounts(accounts);
            saveTransactions(accounts);
        } catch (IOException e) {
            throw new StorageException("Failed to save bank state", e);
        }
    }

    @Override
    public void saveFlaggedAccounts(Set<String> flagged) {
        try {
            saveFlagged(flagged);
        } catch (IOException e) {
            throw new StorageException("Failed to save flagged accounts", e);
        }
    }

    @Override
    public void logAudit(String entry) {
        appendLog(entry); // silent fail by design — logging never crashes the app
    }

    @Override
    public String exportTransactionsCSV(List<Transaction> transactions, String accountNumber) {
        try {
            initialize();
            Path csvFile = DATA_DIR.resolve("transactions_" + accountNumber + ".csv");
            String header = "id,type,amount,balanceAfter,dateTime";
            String rows = transactions.stream()
                    .map(t -> String.join(",",
                            String.valueOf(t.getId()),
                            t.getType().name(),
                            String.valueOf(t.getAmount()),
                            String.valueOf(t.getBalanceAfter()),
                            t.getFileDateTime()
                    ))
                    .collect(Collectors.joining("\n"));
            Files.writeString(csvFile, header + "\n" + rows,
                    StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            System.out.println("📄 Exported to " + csvFile);
            return csvFile.toString();
        } catch (IOException e) {
            throw new StorageException("Failed to export CSV for " + accountNumber, e);
        }
    }

    @Override
    public List<BankAccount> loadAccounts() {
        List<BankAccount> accounts = new ArrayList<>();
        if (!Files.exists(ACCOUNTS_FILE)) {
            System.out.println("ℹ️  No saved data found. Starting fresh.");
            return accounts;
        }
        try {
            List<String> lines = Files.readAllLines(ACCOUNTS_FILE);
            for (String line : lines) {
                if (line.isBlank()) continue;
                try {
                    String[] parts = line.split("\\|");
                    if (parts.length < 5) {
                        System.out.println("⚠️  Skipping malformed line: " + line);
                        continue;
                    }
                    String name          = parts[1];
                    String email         = parts[2];
                    double balance       = Double.parseDouble(parts[3]);
                    AccountStatus status = AccountStatus.valueOf(parts[4]);

                    BankAccount acc = new BankAccount(name, email, balance);
                    if (status == AccountStatus.FROZEN) acc.freeze();
                    else if (status == AccountStatus.CLOSED) acc.close();
                    accounts.add(acc);
                } catch (Exception e) {
                    System.out.println("⚠️  Error parsing line: " + line + " — " + e.getMessage());
                }
            }
            System.out.println("📂 Loaded " + accounts.size() + " accounts from " + ACCOUNTS_FILE);
        } catch (IOException e) {
            throw new StorageException("Failed to load accounts", e);
        }
        return accounts;
    }

    @Override
    public Set<String> loadFlaggedAccounts() {
        Set<String> flagged = new LinkedHashSet<>();
        if (!Files.exists(FLAGGED_FILE)) {
            System.out.println("ℹ️  No flagged accounts found. Starting with empty set.");
            return flagged;
        }
        try {
            // stream + filter + map + forEach — Phase 3 applied to file loading
            Files.readAllLines(FLAGGED_FILE).stream()
                    .filter(line -> !line.isBlank())
                    .map(String::trim)
                    .forEach(flagged::add);
            System.out.println("📂 Loaded " + flagged.size() + " flagged accounts");
        } catch (IOException e) {
            throw new StorageException("Failed to load flagged accounts", e);
        }
        return flagged;
    }
}