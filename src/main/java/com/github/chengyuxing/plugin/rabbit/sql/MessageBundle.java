package com.github.chengyuxing.plugin.rabbit.sql;

import com.intellij.DynamicBundle;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.PropertyKey;

public class MessageBundle extends DynamicBundle {
    private static final MessageBundle instance = new MessageBundle();

    public MessageBundle() {
        super("messages.MessageBundle");
    }

    public static String message(@NotNull @NonNls @PropertyKey(resourceBundle = "messages.MessageBundle") String key, Object... params) {
        return instance.getMessage(key, params);
    }
}
