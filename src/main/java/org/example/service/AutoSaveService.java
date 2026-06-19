package org.example.service;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Background auto-save using a ScheduledExecutorService.
 * Modern replacement for raw Thread + sleep loop.
 *
 * Saves only when there are unsaved changes (dirty flag).
 */
public class AutoSaveService {

    private final AccountService bankService;
    private final long intervalSeconds;
    private final ScheduledExecutorService scheduler;

    public AutoSaveService(AccountService bankService, long intervalSeconds) {
        this.bankService     = bankService;
        this.intervalSeconds = intervalSeconds;
        this.scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "auto-save");
            t.setDaemon(true);      // dies when app exits
            return t;
        });
    }

    public void start() {
        scheduler.scheduleAtFixedRate(
                this::autoSave,        // the task
                intervalSeconds,       // initial delay before first run
                intervalSeconds,       // then repeat every N seconds
                TimeUnit.SECONDS);

        System.out.println("  💾 Auto-save started (every "
                + intervalSeconds + "s, only when changed).");
    }

    // the task that runs on schedule
    private void autoSave() {
        if (bankService.isDirty()) {
            bankService.saveAll();
            System.out.println("  💾 [auto-save] Changes saved.");
        }

    }

    public void stop() {
        scheduler.shutdown();    // stop the scheduler cleanly
        System.out.println("  💾 Auto-save stopped.");
    }
}