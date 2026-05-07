package org.example.service;

import org.example.model.*;

import java.time.LocalDateTime;
import java.util.*;

public class ImmutabilityDemo {

    // ================================================
    // DEMO 1: The Danger — mutable objects leak state
    // ================================================

    public static void demonstrateDanger() {
        System.out.println("--- DANGER: Mutable State Leaks ---");
        System.out.println();

        // ─── Problem 1: Mutable list passed to constructor ───
        System.out.println("1. Mutable input list attack:");

        List<String> tags = new ArrayList<>(List.of("VIP", "Premium"));
        System.out.println("   Tags before: " + tags);

        // If a class stores the reference directly (no defensive copy):
        // class BadAccount { this.tags = tags; }  ← stores the SAME list
        // The caller still has a reference to the SAME list:
        // tags.clear();  ← clears the account's internal tags too! 💀

        // SAFE version: this.tags = List.copyOf(tags);
        List<String> safeTags = List.copyOf(tags);
        tags.clear(); // caller clears their list
        System.out.println("   Tags after clear: " + tags);          // [] — caller's list is empty
        System.out.println("   Safe copy untouched: " + safeTags);   // [VIP, Premium] ✅

        // ─── Problem 2: Mutable return value ───
        System.out.println();
        System.out.println("2. Mutable return value attack:");

        List<String> internalList = new ArrayList<>(List.of("Transaction1", "Transaction2"));

        // BAD: returning the actual internal list
        List<String> leaked = internalList; // simulates: return this.transactions;
        leaked.add("FAKE_TRANSACTION");     // 💀 modifies internal state!
        System.out.println("   Internal after leak attack: " + internalList);
        // [Transaction1, Transaction2, FAKE_TRANSACTION] — corrupted!

        // SAFE: return List.copyOf
        internalList = new ArrayList<>(List.of("Transaction1", "Transaction2"));
        List<String> safe = List.copyOf(internalList);
        try {
            safe.add("FAKE_TRANSACTION"); // ❌ throws UnsupportedOperationException
        } catch (UnsupportedOperationException e) {
            System.out.println("   Safe copy blocked modification ✅");
        }
        System.out.println("   Internal after safe return: " + internalList);
        // [Transaction1, Transaction2] — untouched ✅

        // ─── Problem 3: Shallow copy doesn't protect mutable objects ───
        System.out.println();
        System.out.println("3. Shallow copy trap:");

        // Imagine a mutable Transaction class (hypothetical):
        // MutableTransaction txn = new MutableTransaction(500);
        // List<MutableTransaction> copy = List.copyOf(list);
        // copy.get(0).setAmount(0);  ← CHANGES the original transaction!
        // Because both lists share the SAME MutableTransaction objects.

        System.out.println("   Shallow copy protects the LIST, not the OBJECTS inside.");
        System.out.println("   Solution: make the objects immutable too (all final fields, no setters).");
        System.out.println("   Our Transaction class IS immutable → shallow copy is sufficient ✅");
    }

    // ================================================
    // DEMO 2: ImmutableAccount — truly immutable
    // ================================================

    public static void demonstrateImmutableAccount(List<BankAccount> accounts) {
        System.out.println();
        System.out.println("--- ImmutableAccount: Truly Immutable ---");
        System.out.println();

        if (accounts.isEmpty()) {
            System.out.println("  No accounts to snapshot.");
            return;
        }

        BankAccount liveAccount = accounts.get(0);
        System.out.println("  Live account: " + liveAccount);
        System.out.println("  Live balance: $" + String.format("%,.2f", liveAccount.getBalance()));

        // Take a snapshot — frozen in time
        ImmutableAccount snapshot = ImmutableAccount.snapshotOf(liveAccount);
        System.out.println("  Snapshot:     " + snapshot);

        // Modify the live account
        System.out.println();
        System.out.println("  Depositing $1000 into LIVE account...");
        try {
            liveAccount.deposit(1000);
        } catch (Exception e) {
            System.out.println("  (Account might not be active, skipping deposit)");
        }

        // Snapshot is UNCHANGED — it's a frozen copy
        System.out.println("  Live balance now:     $" + String.format("%,.2f", liveAccount.getBalance()));
        System.out.println("  Snapshot balance:     $" + String.format("%,.2f", snapshot.getBalance()));
        System.out.println("  Snapshot is unchanged ✅ — it's immutable");

        // Try to modify the snapshot's transaction list
        System.out.println();
        System.out.println("  Trying to modify snapshot's transactions:");
        try {
            snapshot.getTransactions().clear();
        } catch (UnsupportedOperationException e) {
            System.out.println("  Blocked! ✅ — transactions list is unmodifiable");
        }

        // "Modify" by creating a NEW snapshot
        System.out.println();
        System.out.println("  Creating modified copy (withBalance):");
        ImmutableAccount modified = snapshot.withBalance(99999.99);
        System.out.println("  Original snapshot: $" + String.format("%,.2f", snapshot.getBalance()));
        System.out.println("  Modified copy:     $" + String.format("%,.2f", modified.getBalance()));
        System.out.println("  Original UNCHANGED ✅ — withBalance returns a NEW object");
    }

    // ================================================
    // DEMO 3: Defensive copying patterns
    // ================================================

    public static void demonstrateDefensiveCopying() {
        System.out.println();
        System.out.println("--- Defensive Copying Patterns ---");
        System.out.println();

        // ─── Pattern 1: Copy on INPUT (constructor) ───
        System.out.println("1. Copy on input (constructor):");
        System.out.println();
        System.out.println("   ❌ BAD:");
        System.out.println("   this.items = items;         // stores caller's reference");
        System.out.println();
        System.out.println("   ✅ GOOD (mutable copy):");
        System.out.println("   this.items = new ArrayList<>(items);  // independent copy");
        System.out.println();
        System.out.println("   ✅ BEST (immutable copy):");
        System.out.println("   this.items = List.copyOf(items);      // can't be modified at all");

        // ─── Pattern 2: Copy on OUTPUT (getter) ───
        System.out.println();
        System.out.println("2. Copy on output (getter):");
        System.out.println();
        System.out.println("   ❌ BAD:");
        System.out.println("   return this.items;          // leaks internal reference");
        System.out.println();
        System.out.println("   ✅ GOOD (mutable copy):");
        System.out.println("   return new ArrayList<>(items);  // caller gets separate copy");
        System.out.println();
        System.out.println("   ✅ BEST (immutable copy):");
        System.out.println("   return List.copyOf(items);      // caller can't modify");
        System.out.println();
        System.out.println("   ✅ ALREADY IMMUTABLE (no copy needed):");
        System.out.println("   return this.items;  // IF items was stored as List.copyOf in constructor");
        System.out.println("                       // AND objects inside are immutable");
        System.out.println("                       // Then returning the reference IS safe");

        // ─── Pattern 3: When you don't need defensive copies ───
        System.out.println();
        System.out.println("3. When defensive copies are NOT needed:");
        System.out.println();
        System.out.println("   • Primitives (int, double, boolean) — copied by value");
        System.out.println("   • Strings — immutable");
        System.out.println("   • Enums (AccountStatus, TransactionType) — immutable");
        System.out.println("   • LocalDateTime, LocalDate — immutable");
        System.out.println("   • Records — immutable by design");
        System.out.println("   • Integer, Double, etc. — immutable wrappers");
        System.out.println();
        System.out.println("   These are ALL safe to return directly — no copies needed.");
    }

    // ================================================
    // DEMO 4: SmartBank audit — where are we safe?
    // ================================================

    public static void auditSmartBankImmutability() {
        System.out.println();
        System.out.println("--- SmartBank Immutability Audit ---");
        System.out.println();

        System.out.println("CLASS                   FIELD                  SAFE?  WHY");
        System.out.println("───────────────────────────────────────────────────────────────────────");
        System.out.println("BankAccount             accountNumber (final)   ✅    String is immutable");
        System.out.println("BankAccount             ownerName (final)       ✅    String is immutable");
        System.out.println("BankAccount             email (final)           ✅    String is immutable");
        System.out.println("BankAccount             balance                 ✅    primitive double");
        System.out.println("BankAccount             status                  ✅    enum is immutable");
        System.out.println("BankAccount             createdAt (final)       ✅    LocalDateTime immutable");
        System.out.println("BankAccount             transactions (final)    ✅    List.copyOf in getter");
        System.out.println();
        System.out.println("Transaction             transactionId (final)   ✅    primitive int");
        System.out.println("Transaction             accountNumber (final)   ✅    String");
        System.out.println("Transaction             type (final)            ✅    enum");
        System.out.println("Transaction             amount (final)          ✅    primitive");
        System.out.println("Transaction             balanceAfter (final)    ✅    primitive");
        System.out.println("Transaction             dateTime (final)        ✅    LocalDateTime");
        System.out.println("Transaction             ALL FIELDS FINAL        ✅    Transaction IS immutable!");
        System.out.println();
        System.out.println("AccountManager          accountList             ⚠️    Mutable internally (by design)");
        System.out.println("AccountManager          accountMap              ⚠️    Mutable internally (by design)");
        System.out.println("AccountManager          getAllAccounts()         ✅    Returns List.copyOf");
        System.out.println();
        System.out.println("Records (Step 29)       ALL fields              ✅    Records are immutable by design");
        System.out.println("  TransferResult        all components final    ✅    Record");
        System.out.println("  AccountSummary        all components final    ✅    Record");
        System.out.println("  MoneyAmount           all components final    ✅    Record + no setters");
        System.out.println();
        System.out.println("CONCLUSION:");
        System.out.println("  Domain objects (Transaction, Records) → fully immutable ✅");
        System.out.println("  Entity (BankAccount) → mutable by design, but controlled ✅");
        System.out.println("  Managers/Services → mutable internally, safe externally ✅");
    }

    // ================================================
    // DEMO 5: The immutability checklist
    // ================================================

    public static void printImmutabilityChecklist() {
        System.out.println();
        System.out.println("╔══════════════════════════════════════════════════════╗");
        System.out.println("║         IMMUTABILITY CHECKLIST                       ║");
        System.out.println("╠══════════════════════════════════════════════════════╣");
        System.out.println("║                                                      ║");
        System.out.println("║  To make a class immutable:                          ║");
        System.out.println("║                                                      ║");
        System.out.println("║  □ 1. Make the class FINAL                          ║");
        System.out.println("║       (prevents subclasses adding mutable fields)    ║");
        System.out.println("║                                                      ║");
        System.out.println("║  □ 2. Make ALL fields PRIVATE and FINAL             ║");
        System.out.println("║       (set once in constructor, never again)         ║");
        System.out.println("║                                                      ║");
        System.out.println("║  □ 3. NO setter methods                             ║");
        System.out.println("║       (no setBalance, no setName, no setAnything)    ║");
        System.out.println("║                                                      ║");
        System.out.println("║  □ 4. DEFENSIVE COPY mutable inputs in constructor  ║");
        System.out.println("║       this.list = List.copyOf(inputList)             ║");
        System.out.println("║       this.date = new Date(inputDate.getTime())      ║");
        System.out.println("║                                                      ║");
        System.out.println("║  □ 5. DEFENSIVE COPY mutable outputs in getters     ║");
        System.out.println("║       return List.copyOf(this.list)                  ║");
        System.out.println("║       OR store as immutable in constructor (better)  ║");
        System.out.println("║                                                      ║");
        System.out.println("║  □ 6. Ensure all contained objects are ALSO immutable║");
        System.out.println("║       List of mutable objects = still mutable!       ║");
        System.out.println("║       List of immutable objects = truly immutable ✅  ║");
        System.out.println("║                                                      ║");
        System.out.println("║  □ 7. Use 'with' methods for modifications           ║");
        System.out.println("║       withBalance(newBal) returns NEW object          ║");
        System.out.println("║       Original is UNCHANGED                          ║");
        System.out.println("║                                                      ║");
        System.out.println("║  SHORTCUT: Use RECORDS for data carriers             ║");
        System.out.println("║    record Point(int x, int y) {}                     ║");
        System.out.println("║    → final class, final fields, no setters           ║");
        System.out.println("║    → Rules 1-3 handled automatically!                ║");
        System.out.println("║                                                      ║");
        System.out.println("╠══════════════════════════════════════════════════════╣");
        System.out.println("║                                                      ║");
        System.out.println("║  ALREADY IMMUTABLE IN JAVA (no copying needed):      ║");
        System.out.println("║    String, Integer, Double, Boolean, Long            ║");
        System.out.println("║    LocalDate, LocalTime, LocalDateTime, Instant      ║");
        System.out.println("║    BigDecimal, BigInteger                            ║");
        System.out.println("║    Enum constants (AccountStatus, TransactionType)   ║");
        System.out.println("║    Records (TransferResult, AccountSummary)          ║");
        System.out.println("║    Optional, List.of(), Set.of(), Map.of()           ║");
        System.out.println("║                                                      ║");
        System.out.println("║  MUTABLE IN JAVA (defensive copy needed):            ║");
        System.out.println("║    Date, Calendar (use java.time instead!)           ║");
        System.out.println("║    ArrayList, HashMap, HashSet                       ║");
        System.out.println("║    Arrays (int[], String[])                          ║");
        System.out.println("║    StringBuilder                                     ║");
        System.out.println("║    Any class with setters                            ║");
        System.out.println("║                                                      ║");
        System.out.println("╚══════════════════════════════════════════════════════╝");
    }

    // ================================================
    // RUN ALL DEMOS
    // ================================================

    public static void runFullDemo(List<BankAccount> accounts) {
        System.out.println();
        System.out.println("╔══════════════════════════════════════════════════════╗");
        System.out.println("║      IMMUTABILITY & DEFENSIVE COPYING DEMO           ║");
        System.out.println("╠══════════════════════════════════════════════════════╣");

        demonstrateDanger();
        System.out.println();
        demonstrateImmutableAccount(accounts);
        System.out.println();
        demonstrateDefensiveCopying();
        System.out.println();
        auditSmartBankImmutability();
        System.out.println();
        printImmutabilityChecklist();
    }
}