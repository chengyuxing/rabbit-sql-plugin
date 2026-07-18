package com.github.chengyuxing.plugin.rabbit.sql.util;

import com.github.chengyuxing.common.util.StringUtils;
import com.github.chengyuxing.plugin.rabbit.sql.MessageBundle;
import com.github.chengyuxing.plugin.rabbit.sql.common.Constants;
import com.github.chengyuxing.plugin.rabbit.sql.common.Global;
import com.github.chengyuxing.plugin.rabbit.sql.common.XQLConfigManager;
import com.intellij.codeInsight.navigation.NavigationUtil;
import com.intellij.ide.fileTemplates.FileTemplateManager;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.module.ModuleUtil;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectUtil;
import com.intellij.openapi.util.Computable;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.psi.*;
import com.intellij.psi.search.GlobalSearchScope;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.io.UncheckedIOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;
import java.util.StringJoiner;

public class ProjectFileUtil {
    private final static Logger log = Logger.getInstance(ProjectFileUtil.class);

    public static Document getDocument(Project project, VirtualFile virtualFile) {
        if (Objects.isNull(virtualFile)) {
            return null;
        }
        var doc = ApplicationManager.getApplication().runReadAction((Computable<Document>) () -> {
            var psi = PsiManager.getInstance(project).findFile(virtualFile);
            if (Objects.isNull(psi)) {
                return null;
            }
            return PsiDocumentManager.getInstance(project).getDocument(psi);
        });
        if (Objects.isNull(doc)) {
            return null;
        }
        return doc;
    }

    public static void openFile(Project project, Path file, boolean refresh) {
        var newVf = refresh ?
                LocalFileSystem.getInstance().refreshAndFindFileByNioFile(file) :
                VirtualFileManager.getInstance().findFileByNioPath(file);
        if (Objects.isNull(newVf)) {
            return;
        }
        ApplicationManager.getApplication().runReadAction(() -> {
            var psi = PsiManager.getInstance(project).findFile(newVf);
            if (Objects.isNull(psi)) {
                return;
            }
            NavigationUtil.activateFileWithPsiElement(psi);
        });
    }

    public static @Nullable Path getProjectPath(Project project) {
        var vf = ProjectUtil.guessProjectDir(project);
        if (vf == null) {
            return null;
        }
        return vf.toNioPath();
    }

    public static boolean isXqlFileManagerConfig(String name) {
        return Constants.CONFIG_PATTERN.matcher(name).matches();
    }

    public static boolean isResourceXqlFileManagerConfig(VirtualFile moduleFv, VirtualFile configVf) {
        var name = configVf.getName();
        if (isXqlFileManagerConfig(name)) {
            var moduleResourcePath = moduleFv.toNioPath().resolve(Constants.RESOURCES_ROOT);
            var configPath = configVf.toNioPath();
            return configPath.startsWith(moduleResourcePath);
        }
        return false;
    }

    public static void loadProjectConfigs(Project project, boolean silent, Runnable foreach) {
        XQLConfigManager xqlConfigManager = XQLConfigManager.getInstance(project);
        var modules = ModuleManager.getInstance(project).getModules();
        if (modules.length > 0) {
            for (Module module : modules) {
                foreach.run();
                loadConfigs(ProjectUtil.guessModuleDir(module), xqlConfigManager, silent);
            }
        } else {
            loadConfigs(ProjectUtil.guessProjectDir(project), xqlConfigManager, silent);
        }
    }

    public static void loadConfigs(VirtualFile moduleVf, XQLConfigManager xqlConfigManager, boolean silent) {
        if (moduleVf == null) return;
        if (!ProjectFileUtil.isResourceProjectModule(moduleVf)) return;
        var resourcesVfs = moduleVf.findFileByRelativePath(Constants.RESOURCE_ROOT_PATH);
        if (resourcesVfs == null || !resourcesVfs.isDirectory()) return;
        var resourcesFiles = resourcesVfs.getChildren();
        boolean found = false;
        for (VirtualFile vf : resourcesFiles) {
            var name = vf.getName();
            if (!ProjectFileUtil.isXqlFileManagerConfig(name)) continue;
            var config = xqlConfigManager.newConfig(moduleVf);
            config.setConfigVfs(vf);
            if (!config.isValid()) continue;
            config.fire(silent);
            xqlConfigManager.add(moduleVf.toNioPath(), config);
            found = true;
        }
        if (!found) {
            var config = xqlConfigManager.newConfig(moduleVf);
            xqlConfigManager.add(moduleVf.toNioPath(), config);
        }
    }

    public static boolean isResourceProjectModule(VirtualFile module) {
        var mPath = module.toNioPath();
        var resourcesPath = mPath.resolve(Constants.RESOURCES_ROOT);
        return Files.exists(resourcesPath);
    }

    public static void createXqlConfigByTemplate(Project project, Path absFilename, Runnable then) {
        ProgressManager.getInstance().run(new Task.Backgroundable(project, MessageBundle.message("ui.dialog.newXqlFileManager.ok.progress"), false) {
            @Override
            public void run(@NotNull ProgressIndicator indicator) {
                indicator.setIndeterminate(true);
                try {
                    var xqlConfig = FileTemplateManager.getInstance(project).getTemplate("XQL File Manager.yml");
                    var path = absFilename.getParent();
                    if (!Files.exists(path)) {
                        Files.createDirectories(path);
                    }
                    var template = xqlConfig.getText(Global.usefulArgs());
                    Files.writeString(absFilename, template, StandardCharsets.UTF_8, StandardOpenOption.CREATE_NEW);
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                }
            }

            @Override
            public void onSuccess() {
                then.run();
            }

            @Override
            public void onThrowable(@NotNull Throwable error) {
                NotificationUtil.showMessage(project, error.getMessage(), NotificationType.ERROR);
                log.warn(error);
            }
        });
    }

    public static VirtualFile findXqlByAlias(String alias, XQLConfigManager.Config config) {
        var resource = config.getXqlFileManager().getResource(alias);
        if (Objects.isNull(resource)) {
            return null;
        }
        var filename = resource.getFilename();
        return VirtualFileManager.getInstance()
                .findFileByNioPath(Path.of(URI.create(filename)));
    }

    public static VirtualFile getValidVirtualFile(VirtualFile file) {
        if (Objects.isNull(file)) return null;
        if (file.isValid()) return file;
        return getValidVirtualFile(file.getParent());
    }

    public static VirtualFile findModule(Project project, VirtualFile fileBelongs) {
        if (Objects.isNull(fileBelongs)) {
            return null;
        }
        var module = ModuleUtil.findModuleForFile(fileBelongs, project);
        if (Objects.isNull(module)) {
            return null;
        }
        return ProjectUtil.guessModuleDir(module);
    }

    public static VirtualFile findModule(PsiElement element) {
        var file = element.getContainingFile();
        if (!file.isPhysical()) {
            file = file.getOriginalFile();
        }
        var vf = file.getVirtualFile();
        return findModule(element.getProject(), vf);
    }

    public static @Nullable Path getModulePath(PsiElement element) {
        return Optional.ofNullable(findModule(element))
                .map(VirtualFile::toNioPath)
                .orElse(null);
    }

    public static long lineNumber(Path path) {
        try (var fr = new FileReader(path.toFile());
             var lr = new LineNumberReader(fr)) {
            //noinspection ResultOfMethodCallIgnored
            lr.skip(Long.MAX_VALUE);
            return lr.getLineNumber();
        } catch (IOException e) {
            return 0;
        }
    }

    public static boolean isURI(String path) {
        return Constants.URI_PATTERN.matcher(path).matches() || StringUtils.startsWithIgnoreCase(path, "file:");
    }

    public static boolean isLocalFileUri(String path) {
        return StringUtils.startsWithIgnoreCase(path, "file:");
    }

    public static Path createJavaFilePath(XQLConfigManager.Config config, String className) {
        var sourceRoot = config.getModulePath()
                .resolve(Constants.JAVA_SOURCE_ROOT);
        if (!className.contains(".")) {
            return sourceRoot.resolve(className + ".java");
        }
        var packages = className.split("\\.");
        return sourceRoot
                .resolve(Path.of(packages[0], Arrays.copyOfRange(packages, 1, packages.length - 1)))
                .resolve(packages[packages.length - 1] + ".java");
    }

    public static String formatPath(Path path) {
        StringJoiner sb = new StringJoiner("/");
        path.forEach(p -> sb.add(p.toString()));
        return sb.toString();
    }

    public static boolean containsWord(VirtualFile file, String id) {
        if (file == null) return false;
        var doc = ApplicationManager.getApplication().runReadAction((Computable<Document>) () -> FileDocumentManager.getInstance().getDocument(file));
        if (doc == null) return false;
        int end = Math.min(doc.getTextLength(), 100);
        String header = doc.getText(new TextRange(0, end));
        return header.contains(id);
    }

    public static boolean isPluginGenerated(VirtualFile file) {
        return containsWord(file, "@RabbitSqlGenerated");
    }

    public static void openJavaFile(Project project, Module module, String className) {
        JavaPsiFacade facade = JavaPsiFacade.getInstance(project);
        var scope = module == null
                ? GlobalSearchScope.allScope(project)
                : GlobalSearchScope.moduleWithDependenciesAndLibrariesScope(module);
        ApplicationManager.getApplication().runReadAction(() -> {
            PsiClass psiClass = facade.findClass(className, scope);
            if (psiClass == null) return;
            psiClass.navigate(true);
        });
    }
}
