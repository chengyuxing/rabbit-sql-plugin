package com.github.chengyuxing.plugin.rabbit.sql.common;

import com.intellij.notification.Notification;
import com.intellij.notification.Notifications;
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
     * Show message.
     *
     * @param project project
     * @param message message
     * @param active  message cache active milliseconds
     */
    public void show(Project project, String title, Message message, int active) {
        if (Objects.isNull(future) || future.isCancelled() || future.isDone()) {
            future = service.schedule(() -> {
                messages.clear();
                future.cancel(true);
                future = null;
            }, active, TimeUnit.MILLISECONDS);
        }
        if (messages.isEmpty() || !messages.contains(message)) {
            messages.add(message);
            var notice = new Notification("Rabbit-SQL Notification Group", title, message.getText(), message.getType());
            Notifications.Bus.notify(notice, project);
        }
    }

    /**
     * Show message (message cache active 5000 milliseconds).
     *
     * @param project project
     * @param message message
     */
    public void show(Project project, String title, Message message) {
        show(project, title, message, 5000);
    }

    /**
     * Show message with title 'XQL file manager' (message cache active 5000 milliseconds).
     *
     * @param project project
     * @param message message
     */
    public void show(Project project, Message message) {
        show(project, "XQL file manager", message);
    }
}
