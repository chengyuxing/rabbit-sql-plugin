package com.github.chengyuxing.plugin.rabbit.sql.common;

import com.github.chengyuxing.plugin.rabbit.sql.plugins.java.extensions.NewXqlIfNotExists;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.roots.ModuleRootModificationUtil;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiLiteralExpression;
import com.intellij.psi.PsiManager;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.testFramework.HeavyPlatformTestCase;

import java.nio.file.Files;
import java.nio.file.Path;

public class NewXqlIfNotExistsTest extends HeavyPlatformTestCase {
    private Path modulePath;
    private VirtualFile sqlFile;
    private final NewXqlIfNotExists action = new NewXqlIfNotExists();
    private int sourceIndex;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        modulePath = Path.of(getProject().getProjectFilePath()).getParent();
        var module = VfsUtil.createDirectories(modulePath.toString());
        ModuleRootModificationUtil.addContentRoot(getModule(), modulePath.toString());
        var resources = Files.createDirectories(modulePath.resolve("src/main/resources"));
        var sql = Files.writeString(resources.resolve("home.xql"), "/* [find] */\nselect 1;\n");
        var yaml = Files.writeString(resources.resolve("xql-file-manager.yml"), "files:\n  home: home.xql\n");
        var fileSystem = LocalFileSystem.getInstance();
        sqlFile = fileSystem.refreshAndFindFileByNioFile(sql);
        var yamlFile = fileSystem.refreshAndFindFileByNioFile(yaml);
        assertNotNull(sqlFile);
        assertNotNull(yamlFile);
        var manager = XQLConfigManager.getInstance(getProject());
        var config = manager.newConfig(module);
        config.setConfigVfs(yamlFile);
        config.initXqlFileManager();
        manager.add(modulePath, config);
        assertTrue(config.getXqlFileManager().contains("home.find"));
    }

    public void testExistingFragmentDoesNotOfferCreationIncludingReferenceModifiers() throws Exception {
        for (String reference : new String[]{"&home.find", "&home.find^count", "&home.find^page"}) {
            assertFalse(reference, action.isAvailable(getProject(), null, literal(reference)));
        }
    }

    public void testMissingFragmentOrAliasStillOffersCreation() throws Exception {
        assertTrue(action.isAvailable(getProject(), null, literal("&home.missing")));
        assertTrue(action.isAvailable(getProject(), null, literal("&missing.find")));
        assertFalse(action.isAvailable(getProject(), null, literal("home.find")));
        assertFalse(action.isAvailable(getProject(), null, literal("select 1")));
    }

    public void testInvokingAnExistingFragmentDoesNotAppendDuplicateSql() throws Exception {
        var document = FileDocumentManager.getInstance().getDocument(sqlFile);
        assertNotNull(document);
        var original = document.getText();
        action.invoke(getProject(), null, literal("&home.find"));
        action.invoke(getProject(), null, literal("&home.find^count"));
        assertEquals(original, document.getText());
        assertFalse(FileDocumentManager.getInstance().isDocumentUnsaved(document));
    }

    private PsiLiteralExpression literal(String reference) throws Exception {
        var name = "Example" + sourceIndex++;
        var path = Files.writeString(modulePath.resolve(name + ".java"),
                "class " + name + " { String sql = \"" + reference + "\"; }");
        var file = LocalFileSystem.getInstance().refreshAndFindFileByNioFile(path);
        assertNotNull(file);
        var psi = PsiManager.getInstance(getProject()).findFile(file);
        assertNotNull(psi);
        var literal = PsiTreeUtil.findChildOfType(psi, PsiLiteralExpression.class);
        assertNotNull(literal);
        return literal;
    }
}
