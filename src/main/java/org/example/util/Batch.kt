package org.example.util

import java.time.Instant
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit

class Batch private constructor() {
    init {
        throw IllegalStateException("Utility class")
    }

    companion object {
        const val MAX: Int = 1
        private val scheduler: ScheduledExecutorService = Executors.newScheduledThreadPool(1)

        /**
         * @param minutes minutes to sleep until processing
         */
        @JvmStatic
        @Synchronized
        fun process(minutes: Int, runnable: Runnable) {
            scheduler.scheduleAtFixedRate(Runnable {
                try {
                    println("Starting batch processing... at " + Instant.now())
                    runnable.run()
                } catch (e: Exception) {
                    System.err.println("Error processing batch at: " + Instant.now() + " - " + e.message)
                }
            }, minutes.toLong(), minutes.toLong(), TimeUnit.MINUTES)
        }
    }
}