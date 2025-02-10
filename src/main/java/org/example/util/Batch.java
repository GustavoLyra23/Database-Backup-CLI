package org.example.util;

import java.time.Instant;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Batch {

    static final int MAX = 1;
    private static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    private Batch() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * @param minutes minutes to sleep until processing
     */
    public synchronized static void process(final int minutes, final Runnable runnable) {
        scheduler.scheduleAtFixedRate(() -> {
            try {
                System.out.println("Starting batch processing... at " + Instant.now());
                runnable.run();
            } catch (Exception e) {
                System.err.println("Error processing batch at: " + Instant.now() + " - " + e.getMessage());
            }
        }, minutes, minutes, TimeUnit.MINUTES);
    }
}