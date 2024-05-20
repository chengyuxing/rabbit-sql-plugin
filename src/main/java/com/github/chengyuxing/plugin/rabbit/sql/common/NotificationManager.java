package com.github.chengyuxing.plugin.rabbit.sql.common;

import com.github.chengyuxing.plugin.rabbit.sql.util.NotificationUtil;
import com.intellij.openapi.project.Project;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public final class NotificationManager {
    private static volatile NotificationManager instance;
    private final Set<Message> messages = new HashSet<>();
    private final ScheduledExecutorService service = Executors.newSingleThreadScheduledExecutor();
    private ScheduledFuture<?> future;

    private NotificationManager() {
    }

    public static NotificationManager getInstance() {
        if (instance == null) {
            synchronized (XQLConfigManager.class) {
                if (instance == null) {
                    instance = new NotificationManager();
                }
            }
        }
        return instance;
    }

    /**
     * Show System message.
     *
     * @param project project
     * @param message message
     * @param active  message cache active milliseconds
     */
    public void show(Project project, Message message, int active) {
        if (Objects.isNull(future) || future.isCancelled() || future.isDone()) {
            future = service.schedule(() -> {
                messages.clear();
                future.cancel(true);
                future = null;
            }, active, TimeUnit.MILLISECONDS);
        }
        if (messages.isEmpty() || !messages.contains(message)) {
            messages.add(message);
            NotificationUtil.showMessage(project, message.getText(), message.getType());
        }
    }

    /**
     * Show System message (message cache active 3000 milliseconds).
     *
     * @param project project
     * @param message message
     */
    public void show(Project project, Message message) {
        show(project, message, 3000);
    }
}
