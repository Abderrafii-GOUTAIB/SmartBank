package org.example.service;
/**
 * Singleton — one config object for the entire bank.
 * Centralizes all bank-wide constants in one place.
 * Before: BANK_NAME in BankAccount, LARGE_TRANSACTION_THRESHOLD
 *         in RiskService — scattered, hard to change consistently.
 * After:  one place to update any bank-wide setting.
 */
public class BankConfig {

    // ── THE INSTANCE ──────────────────────────────────────────────────────────
    // private — nobody outside can touch it directly
    // static  — belongs to the class, not to any object
    // volatile — guarantees visibility across threads (Phase 5 preview)
    private static volatile BankConfig instance;

    // ── BANK SETTINGS ─────────────────────────────────────────────────────────
    private final String  bankName;
    private final double  largeTransactionThreshold;
    private final double  minimumOpeningDeposit;
    private final int     maxTransactionsPerDay;

    // ── PRIVATE CONSTRUCTOR ───────────────────────────────────────────────────
    // private = nobody can call new BankConfig() from outside
    // this is the lock that enforces ONE instance
    private BankConfig() {
        this.bankName                  = "SmartBank";
        this.largeTransactionThreshold = 10_000;
        this.minimumOpeningDeposit     = 0;
        this.maxTransactionsPerDay     = 100;
    }

    // ── THE GATE ──────────────────────────────────────────────────────────────
    // The only way to get the instance.
    // First call: creates it. Every call after: returns the same one.
    //
    // Double-checked locking pattern:
    //   - first check (no sync): fast path, avoids locking after init
    //   - synchronized block: only one thread can create the instance
    //   - second check (inside sync): in case two threads passed first check
    public static BankConfig getInstance() {
        if (instance == null) {                         // first check — no lock
            synchronized (BankConfig.class) {           // only one thread enters
                if (instance == null) {                 // second check — inside lock
                    instance = new BankConfig();
                }
            }
        }
        return instance;
    }

    // ── GETTERS ───────────────────────────────────────────────────────────────
    // final fields + no setters = immutable after creation (Step 31)
    public String getBankName()                  { return bankName; }
    public double getLargeTransactionThreshold() { return largeTransactionThreshold; }
    public double getMinimumOpeningDeposit()     { return minimumOpeningDeposit; }
    public int    getMaxTransactionsPerDay()     { return maxTransactionsPerDay; }

    @Override
    public String toString() {
        return String.format(
                "BankConfig[name=%s, largeThreshold=$%,.0f, minDeposit=$%.2f, maxTxns=%d]",
                bankName, largeTransactionThreshold, minimumOpeningDeposit, maxTransactionsPerDay);
    }
}