package com.github.chengyuxing.plugin.rabbit.sql.common;

import com.intellij.notification.NotificationType;

public class Message {
    private final String text;
    private NotificationType type = NotificationType.INFORMATION;

    public Message(String text) {
        this.text = text;
    }

    public static Message info(String text) {
        var msg = new Message(text);
        msg.setType(NotificationType.INFORMATION);
        return msg;
    }

    public static Message warning(String text) {
        var msg = new Message(text);
        msg.setType(NotificationType.WARNING);
        return msg;
    }

    public static Message error(String text) {
        var msg = new Message(text);
        msg.setType(NotificationType.ERROR);
        return msg;
    }

    public String getText() {
        return text;
    }

    public NotificationType getType() {
        return type;
    }

    public void setType(NotificationType type) {
        this.type = type;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Message)) return false;

        Message message = (Message) o;

        if (!getText().equals(message.getText())) return false;
        return getType() == message.getType();
    }

    @Override
    public int hashCode() {
        int result = getText().hashCode();
        result = 31 * result + getType().hashCode();
        return result;
    }
}
