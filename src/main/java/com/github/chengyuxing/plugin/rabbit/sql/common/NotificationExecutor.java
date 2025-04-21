package com.github.chengyuxing.plugin.rabbit.sql.common;

import java.util.Collection;
import java.util.Set;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

public class NotificationExecutor implements AutoCloseable {
    private final ScheduledExecutorService service;
    private final long delay;
    private final Consumer<Set<Message>> consumer;
    private final AtomicReference<ScheduledFuture<?>> currentRef = new AtomicReference<>();
    private final Set<Message> messages = ConcurrentHashMap.newKeySet();

    public NotificationExecutor(Consumer<Set<Message>> consumer, long delay) {
        this.delay = delay;
        this.consumer = consumer;
        service = Executors.newSingleThreadScheduledExecutor();
    }

    public void show(Message message) {
        messages.add(message);
        trigger();
    }

    public void show(Collection<Message> messages) {
        this.messages.addAll(messages);
        trigger();
    }

    void trigger() {
        var current = this.currentRef.get();
        if (current != null && (!current.isCancelled() || !current.isDone())) {
            current.cancel(false);
            currentRef.set(null);
        }
        var newCurrent = service.schedule(() -> {
            consumer.accept(messages);
            messages.clear();
        }, delay, TimeUnit.MILLISECONDS);
        currentRef.set(newCurrent);
    }

    @Override
    public void close() {
        try {
            service.shutdown();
            messages.clear();
        } catch (Exception ignore) {
        }
    }
}
