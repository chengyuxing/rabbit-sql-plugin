package com.github.chengyuxing.plugin.rabbit.sql.util;

import java.nio.file.Path;

public class PathUtil {
    public static Path backward(Path path, int steps) {
        if (path.getParent() == null) return path;
        if (steps <= 0) return path;
        return backward(path.getParent(), --steps);
    }
}
