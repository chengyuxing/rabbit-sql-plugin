package com.github.chengyuxing.plugin.rabbit.sql.util;

import com.github.chengyuxing.common.io.FileResource;
import com.github.chengyuxing.plugin.rabbit.sql.common.Constants;
import com.github.chengyuxing.sql.XQLFileManagerConfig;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.psi.PsiFile;

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

    public static boolean isXqlFileManagerConfig(PsiFile psiFile) {
        if (psiFile == null) return false;
        if (!psiFile.isPhysical()) return false;
        if (!psiFile.isValid()) return false;
        var vf = psiFile.getVirtualFile();
        if (vf == null) return false;
        var path = vf.getFileSystem().getNioPath(vf);
        if (path == null) return false;
        var config = new XQLFileManagerConfig();
        try {
            config.loadYaml(new FileResource(path.toUri().toString()));
            return true;
        } catch (Exception e) {
            log.warn(e);
            return false;
        }
    }
}
