package com.github.chengyuxing.plugin.rabbit.sql.common;

import com.github.chengyuxing.common.MostDateTime;
import com.github.chengyuxing.sql.Args;
import com.intellij.openapi.editor.colors.EditorColorsManager;
import com.intellij.util.ui.UIUtil;

import java.awt.*;

public final class Global {
    /**
     * System (USER, DATE, TIME)
     *
     * @return args
     */
    public static Args<Object> usefulArgs() {
        var now = MostDateTime.now();
        return Args.of(
                "USER", System.getProperty("user.name"),
                "DATE", now.toString("yyyy/MM/dd"),
                "TIME", now.toString("HH:mm:ss")
        );
    }

    public static Font getEditorFont(int size) {
        var editorFont = EditorColorsManager.getInstance().getGlobalScheme().getEditorFontName();
        return UIUtil.getFontWithFallback(new Font(editorFont, Font.PLAIN, size));
    }
}
