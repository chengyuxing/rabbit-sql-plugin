package com.github.chengyuxing.plugin.rabbit.sql.common;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.concurrent.*;
import java.util.function.Consumer;

public class NotificationExecutor implements AutoCloseable {
    private final ScheduledExecutorService service;
    private final long delay;
    private final Consumer<Set<Message>> consumer;
    private ScheduledFuture<?> current;
    private long generation;
    private final Set<Message> messages = new LinkedHashSet<>();

    public NotificationExecutor(Consumer<Set<Message>> consumer, long delay) {
        this.delay = delay;
        this.consumer = consumer;
        service = Executors.newSingleThreadScheduledExecutor();
    }

    public void show(Message message) {
        show(Set.of(message));
    }

    public synchronized void show(Collection<Message> messages) {
        if (service.isShutdown() || messages.isEmpty()) {
            return;
        }
        this.messages.addAll(messages);
        if (current != null) {
            current.cancel(false);
        }
        long scheduledGeneration = ++generation;
        current = service.schedule(() -> flush(scheduledGeneration), delay, TimeUnit.MILLISECONDS);
    }

    private void flush(long scheduledGeneration) {
        Set<Message> batch;
        synchronized (this) {
            if (service.isShutdown() || scheduledGeneration != generation) {
                return;
            }
            current = null;
            batch = Set.copyOf(messages);
            messages.clear();
        }
        if (!batch.isEmpty()) {
            consumer.accept(batch);
        }
    }

    @Override
    public synchronized void close() {
        service.shutdownNow();
        current = null;
        messages.clear();
    }
}
