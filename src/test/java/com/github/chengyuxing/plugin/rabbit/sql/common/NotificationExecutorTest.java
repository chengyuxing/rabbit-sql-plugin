package com.github.chengyuxing.plugin.rabbit.sql.common;

import org.junit.Test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.*;

public class NotificationExecutorTest {
    @Test
    public void concurrentMessagesArrivingDuringConsumptionReachNextBatch() throws Exception {
        var started = new CountDownLatch(1);
        var release = new CountDownLatch(1);
        var delivered = new CountDownLatch(32);
        var batches = new CopyOnWriteArrayList<Set<Message>>();
        var first = Message.info("first");
        var executor = new NotificationExecutor(batch -> {
            batches.add(new HashSet<>(batch));
            if (batch.contains(first)) {
                started.countDown();
                try {
                    release.await(5, TimeUnit.SECONDS);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            } else {
                batch.forEach(message -> delivered.countDown());
            }
        }, 10);
        var senders = Executors.newFixedThreadPool(8);
        try {
            executor.show(first);
            assertTrue(started.await(5, TimeUnit.SECONDS));
            var expected = new HashSet<Message>();
            expected.add(first);
            var tasks = new ArrayList<Future<?>>();
            for (int i = 0; i < 32; i++) {
                var message = Message.info("message-" + i);
                expected.add(message);
                tasks.add(senders.submit(() -> executor.show(Set.of(message))));
            }
            for (var task : tasks) {
                task.get(5, TimeUnit.SECONDS);
            }
            release.countDown();
            assertTrue(delivered.await(5, TimeUnit.SECONDS));
            assertEquals(Set.of(first), batches.get(0));
            var actual = new HashSet<Message>();
            batches.forEach(actual::addAll);
            assertEquals(expected, actual);
        } finally {
            release.countDown();
            senders.shutdownNow();
            executor.close();
        }
    }

    @Test
    public void lateMessagesAfterCloseAreIgnored() {
        var callbacks = new AtomicInteger();
        var executor = new NotificationExecutor(batch -> callbacks.incrementAndGet(), 10);
        executor.close();
        executor.show(Message.info("late"));
        executor.show(Set.of(Message.warning("also late")));
        executor.close();
        assertEquals(0, callbacks.get());
    }
}
