package com.github.chengyuxing.plugin.rabbit.sql.common;

import com.github.chengyuxing.plugin.rabbit.sql.ui.NewPipeDialog;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.project.Project;
import javax.swing.JComponent;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.testFramework.HeavyPlatformTestCase;
import com.intellij.ui.components.JBTextField;

import javax.swing.JLabel;
import java.awt.Component;
import java.awt.Container;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class NewPipeDialogRegressionTest extends HeavyPlatformTestCase {
    public void testInitialPreviewUsesAPortableFileNameWithoutCreatingAFile() throws Exception {
        Path modulePath = Path.of(getProject().getProjectFilePath()).getParent();
        var directory = VfsUtil.createDirectories(modulePath.toString());
        var config = XQLConfigManager.getInstance(getProject()).newConfig(directory);
        var dialog = new TestDialog(getProject(), config);
        try {
            assertPortablePreview(dialog);
            assertFalse(dialog.isOKActionEnabled());
            assertFalse(Files.exists(modulePath.resolve("src/main/java")));
        } finally {
            dialog.close(DialogWrapper.CANCEL_EXIT_CODE);
        }
    }

    public void testValidAndInvalidClassInputsUpdatePreviewWithoutResolvingIllegalNames() throws Exception {
        Path modulePath = Path.of(getProject().getProjectFilePath()).getParent();
        var directory = VfsUtil.createDirectories(modulePath.toString());
        var config = XQLConfigManager.getInstance(getProject()).newConfig(directory);
        var dialog = new TestDialog(getProject(), config);
        try {
            var fields = new ArrayList<JBTextField>();
            collect(dialog.form(), JBTextField.class, fields);
            assertEquals(2, fields.size());
            fields.get(0).setText("demo.UpperPipe");
            assertTrue(dialog.isOKActionEnabled());
            assertPortablePreview(dialog);
            assertTrue(labelText(dialog).contains("src/main/java/demo/UpperPipe.java"));
            fields.get(0).setText("<Invalid>");
            assertFalse(dialog.isOKActionEnabled());
            fields.get(0).setText("");
            assertFalse(dialog.isOKActionEnabled());
            assertFalse(Files.exists(modulePath.resolve("src/main/java")));
        } finally {
            dialog.close(DialogWrapper.CANCEL_EXIT_CODE);
        }
    }

    private static class TestDialog extends NewPipeDialog {
        TestDialog(Project project, XQLConfigManager.Config config) { super(project, config); }
        JComponent form() { return createCenterPanel(); }
    }

    private void assertPortablePreview(TestDialog dialog) {
        var matcher = Pattern.compile("src/main/java/(.*?)\\.java").matcher(labelText(dialog));
        assertTrue("Java file preview not found", matcher.find());
        String name = matcher.group(1);
        assertTrue("Preview contains a non-Java name: " + name,
                Constants.PACKAGE_PATTERN.matcher(name.replace('/', '.')).matches());
    }

    private String labelText(TestDialog dialog) {
        var labels = new ArrayList<JLabel>();
        collect(dialog.form(), JLabel.class, labels);
        var text = new StringBuilder();
        labels.forEach(label -> text.append(label.getText()).append('\n'));
        return text.toString();
    }

    private <T extends Component> void collect(Component component, Class<T> type, List<T> result) {
        if (type.isInstance(component)) result.add(type.cast(component));
        if (component instanceof Container container) {
            for (Component child : container.getComponents()) collect(child, type, result);
        }
    }
}
