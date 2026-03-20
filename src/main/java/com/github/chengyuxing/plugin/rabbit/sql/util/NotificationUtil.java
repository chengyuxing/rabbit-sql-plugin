package com.github.chengyuxing.plugin.rabbit.sql.util;

import com.github.chengyuxing.plugin.rabbit.sql.MessageBundle;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.project.Project;

public class NotificationUtil {
    public static void showMessage(Project project, String title, String message, NotificationType type) {
        var notice = new Notification("Rabbit-SQL Notification Group", title, message, type);
        Notifications.Bus.notify(notice, project);
    }

    public static void showMessage(Project project, String message, NotificationType type) {
        showMessage(project, MessageBundle.message("notice.title"), message, type);
    }
}
