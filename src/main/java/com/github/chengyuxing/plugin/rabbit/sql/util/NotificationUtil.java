package com.github.chengyuxing.plugin.rabbit.sql.util;

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
        showMessage(project, "XQL File Manager", message, type);
    }
}
