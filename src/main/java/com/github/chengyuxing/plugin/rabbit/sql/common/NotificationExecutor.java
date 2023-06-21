package com.github.chengyuxing.plugin.rabbit.sql.common;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class NotificationExecutor implements AutoCloseable {
    private final ScheduledExecutorService service;
    private final long delay;
    private final Consumer<Set<Message>> consumer;
    private ScheduledFuture<?> current;
    private final Set<Message> messageCache = new HashSet<>();

    public NotificationExecutor(Consumer<Set<Message>> consumer, long delay) {
        this.delay = delay;
        this.consumer = consumer;
        service = Executors.newSingleThreadScheduledExecutor();
    }

    public void addMessage(Message message) {
        messageCache.add(message);
        trigger();
    }

    public void addMessages(Collection<Message> messages) {
        this.messageCache.addAll(messages);
        trigger();
    }

    protected void trigger() {
        if (current != null && (!current.isCancelled() || !current.isDone())) {
            current.cancel(false);
            current = null;
        }
        current = service.schedule(() -> {
            consumer.accept(messageCache);
            messageCache.clear();
        }, delay, TimeUnit.MILLISECONDS);
    }

    @Override
    public void close() {
        try {
            service.shutdown();
            messageCache.clear();
        } catch (Exception ignore) {
        }
    }
}
