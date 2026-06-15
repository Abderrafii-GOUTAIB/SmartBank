package org.example.service;

/**
 * Runs a background thread that saves the bank state
 * at a fixed interval, so a crash never loses more than
 * one interval of work.
 *
 * Uses Runnable (Step 46) on a daemon thread.
 */
public class AutoSaveService {

    private final AccountService bankService;
    private final long intervalSeconds;
    private Thread worker;
    private volatile boolean running = false;

    public AutoSaveService(AccountService bankService, long intervalSeconds) {
        this.bankService     = bankService;
        this.intervalSeconds = intervalSeconds;
    }

    // ── START ─────────────────────────────────────────────────────────────────
    public void start() {
        if (running) {
            System.out.println("  ℹ️  Auto-save already running.");
            return;
        }
        running = true;

        worker = new Thread(this::loop, "auto-save");
        worker.setDaemon(true);
        worker.start();

        System.out.println("  💾 Auto-save started (every "
                + intervalSeconds + "s).");
    }

    // ── THE BACKGROUND LOOP ─────────────────────────────────────────────────────
    private void loop() {
        while (running) {
            try {
                Thread.sleep(intervalSeconds * 1000);

                if (running && bankService.isDirty()) {    // ← only if dirty
                    bankService.saveAll();
                    System.out.println("  💾 [auto-save] Changes saved.");
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }


    public void stop() {
        running = false;
        if (worker != null) {
            worker.interrupt();
        }
        System.out.println("  💾 Auto-save stopped.");
    }
}