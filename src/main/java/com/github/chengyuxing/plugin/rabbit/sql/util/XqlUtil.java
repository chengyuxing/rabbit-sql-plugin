package com.github.chengyuxing.plugin.rabbit.sql.util;

import com.github.chengyuxing.plugin.rabbit.sql.common.Constants;
import com.intellij.openapi.diagnostic.Logger;

import java.nio.file.Files;
import java.nio.file.Path;

public class XqlUtil {
    private static final Logger log = Logger.getInstance(XqlUtil.class);

    public static Path getModuleBaseDir(Path xqlFileManagerLocation) {
        if (xqlFileManagerExists(xqlFileManagerLocation)) {
            return xqlFileManagerLocation.getParent().getParent().getParent().getParent();
        }
        return null;
    }

    public static boolean xqlFileManagerExists(Path xqlFileManagerLocation) {
        if (!xqlFileManagerLocation.endsWith(Constants.CONFIG_PATH)) {
            return false;
        }
        if (!Files.exists(xqlFileManagerLocation)) {
            log.warn("cannot find " + Constants.CONFIG_NAME);
            return false;
        }
        return true;
    }
}
