package com.coen448.taskprocessor;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Thread-safe task processor using ExecutorService.
 * Non-blocking API for task submission.
 */
public class TaskProcessor implements AutoCloseable {
    private final ExecutorService executor;
    private final AtomicInteger tasksSubmitted = new AtomicInteger(0);
    private final AtomicInteger tasksProcessed = new AtomicInteger(0);
    private volatile boolean shutdown = false;

    /**
     * Create TaskProcessor with fixed thread pool.
     * @param poolSize number of worker threads
     */
    public TaskProcessor(int poolSize) {
        this.executor = Executors.newFixedThreadPool(
            poolSize,
            new ThreadFactory() {
                private final AtomicInteger count = new AtomicInteger(0);
                @Override
                public Thread newThread(Runnable r) {
                    Thread t = new Thread(r);
                    t.setName("TaskProcessor-Worker-" + count.incrementAndGet());
                    t.setDaemon(false);
                    return t;
                }
            }
        );
        System.out.println("[TaskProcessor] Initialized with " + poolSize + " workers");
    }

    /**
     * Submit a task with a name. Non-blocking.
     * @param taskName descriptive name for logging
     * @param task the Runnable to execute
     * @throws IllegalStateException if processor is shut down
     */
    public void submitTask(String taskName, Runnable task) {
        if (shutdown) {
            throw new IllegalStateException("TaskProcessor is shut down");
        }
        tasksSubmitted.incrementAndGet();
        executor.submit(() -> {
            System.out.println("[" + Thread.currentThread().getName() + "] Starting: " + taskName);
            try {
                task.run();
            } catch (Exception e) {
                System.err.println("[" + Thread.currentThread().getName() + "] Error in " + taskName + ": " + e.getMessage());
            } finally {
                tasksProcessed.incrementAndGet();
                System.out.println("[" + Thread.currentThread().getName() + "] Completed: " + taskName);
            }
        });
    }

    /**
     * Get number of submitted tasks.
     */
    public int getTasksSubmitted() {
        return tasksSubmitted.get();
    }

    /**
     * Get number of completed tasks.
     */
    public int getTasksProcessed() {
        return tasksProcessed.get();
    }

    /**
     * Gracefully shutdown the processor, waiting for pending tasks.
     * @param timeoutSeconds max time to wait
     * @return true if all tasks completed, false if timeout
     */
    public boolean shutdown(long timeoutSeconds) {
        shutdown = true;
        System.out.println("[TaskProcessor] Shutting down...");
        executor.shutdown();
        try {
            boolean completed = executor.awaitTermination(timeoutSeconds, TimeUnit.SECONDS);
            if (completed) {
                System.out.println("[TaskProcessor] Shutdown complete. Processed: " + tasksProcessed.get());
            } else {
                System.out.println("[TaskProcessor] Shutdown timeout; forcing shutdown");
                executor.shutdownNow();
            }
            return completed;
        } catch (InterruptedException e) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
            return false;
        }
    }

    @Override
    public void close() {
        shutdown(10);
    }
}