package org.example.service;

import org.example.model.exception.BankException;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Keeps a history of executed commands.
 * Supports undo of last command.
 *
 * Uses Deque — Java's stack/queue (Phase 2 collections).
 * Stores commands in execution order, undo pops the top.
 *
 * StoragePort injected for audit logging — DIP ✅
 */
public class CommandHistory {

    private final Deque<BankCommand> history = new ArrayDeque<>();
    private final StoragePort storage;

    public CommandHistory(StoragePort storage) {
        this.storage = storage;
    }

    public void executeAndRecord(BankCommand command) {
        try {
            command.execute();
            history.push(command);
            storage.logAudit("CMD_EXECUTED: " + command.description());
        } catch (BankException e) {
            storage.logAudit("CMD_FAILED: " + command.description()
                    + " — " + e.getMessage());
            throw e;
        }
    }


    public void undoLast() {
        if (history.isEmpty()) {
            System.out.println("  ℹ️  Nothing to undo.");
            return;
        }
        BankCommand last = history.pop();
        System.out.println("  ↩️  Undoing: " + last.description());
        last.undo();
        storage.logAudit("CMD_UNDONE: " + last.description());
    }

    public int size() {
        return history.size();
    }

    public boolean isEmpty() {
        return history.isEmpty();
    }


    public void printHistory() {
        System.out.println();
        System.out.println("--- Command History ---");

        if (history.isEmpty()) {
            System.out.println("  No commands executed yet.");
            return;
        }

        // history.stream() iterates newest first (Deque semantics)
        String list = history.stream()
                .map(BankCommand::description)
                .collect(Collectors.joining("\n  "));
        System.out.println("  " + list);
        System.out.println("  (" + history.size() + " command(s))");
    }
}