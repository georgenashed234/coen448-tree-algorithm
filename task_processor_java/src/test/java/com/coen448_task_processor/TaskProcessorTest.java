package com.coen448.taskprocessor;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("TaskProcessor - Thread-Safe Concurrent Task Handling")
public class TaskProcessorTest {

    private TaskProcessor processor;

    @BeforeEach
    void setUp() {
        processor = new TaskProcessor(4);
    }

    @Test
    @DisplayName("Test 1: Single task submission and completion")
    void testSingleTaskSubmission() {
        AtomicInteger counter = new AtomicInteger(0);
        processor.submitTask("test-task", counter::incrementAndGet);
        
        try {
            Thread.sleep(100); // brief wait for async execution
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        assertEquals(1, processor.getTasksSubmitted());
        assertEquals(1, processor.getTasksProcessed());
        assertEquals(1, counter.get());
        processor.shutdown(5);
    }

    @Test
    @DisplayName("Test 2: 100 concurrent task submissions with race-condition check")
    void testConcurrentTaskSubmissions() throws InterruptedException {
        int taskCount = 100;
        AtomicInteger executedCount = new AtomicInteger(0);
        CountDownLatch latch = new CountDownLatch(taskCount);

        for (int i = 0; i < taskCount; i++) {
            final int taskId = i;
            processor.submitTask("concurrent-task-" + taskId, () -> {
                executedCount.incrementAndGet();
                latch.countDown();
            });
        }

        // Wait for all tasks to complete
        boolean completed = latch.await(10, java.util.concurrent.TimeUnit.SECONDS);
        assertTrue(completed, "Not all tasks completed within timeout");

        assertEquals(taskCount, processor.getTasksSubmitted());
        assertEquals(taskCount, processor.getTasksProcessed());
        assertEquals(taskCount, executedCount.get(), "Race condition detected: counts don't match");
        
        processor.shutdown(5);
    }

    @Test
    @DisplayName("Test 3: Task submission after shutdown throws exception")
    void testSubmitAfterShutdown() {
        processor.submitTask("task-1", () -> {});
        processor.shutdown(5);

        assertThrows(IllegalStateException.class, () -> {
            processor.submitTask("task-2", () -> {});
        }, "Should throw exception when submitting after shutdown");
    }

    @Test
    @DisplayName("Test 4: Multiple threads submitting tasks concurrently")
    void testMultipleThreadSubmitters() throws InterruptedException {
        int threadCount = 10;
        int tasksPerThread = 10;
        CountDownLatch submissionLatch = new CountDownLatch(threadCount);
        CountDownLatch completionLatch = new CountDownLatch(threadCount * tasksPerThread);

        for (int t = 0; t < threadCount; t++) {
            new Thread(() -> {
                try {
                    for (int i = 0; i < tasksPerThread; i++) {
                        processor.submitTask("thread-task", () -> {
                            completionLatch.countDown();
                        });
                    }
                } finally {
                    submissionLatch.countDown();
                }
            }).start();
        }

        submissionLatch.await();
        boolean allCompleted = completionLatch.await(15, java.util.concurrent.TimeUnit.SECONDS);
        assertTrue(allCompleted, "Not all tasks completed");

        assertEquals(threadCount * tasksPerThread, processor.getTasksSubmitted());
        assertEquals(threadCount * tasksPerThread, processor.getTasksProcessed());
        
        processor.shutdown(5);
    }

    @Test
    @DisplayName("Test 5: Shutdown with timeout verification")
    void testShutdownWithTimeout() {
        CountDownLatch latch = new CountDownLatch(5);
        
        for (int i = 0; i < 5; i++) {
            processor.submitTask("slow-task-" + i, () -> {
                try {
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                latch.countDown();
            });
        }

        boolean completed = processor.shutdown(10);
        assertTrue(completed, "Shutdown should complete within 10 seconds");
        assertEquals(5, processor.getTasksProcessed());
    }

    
}