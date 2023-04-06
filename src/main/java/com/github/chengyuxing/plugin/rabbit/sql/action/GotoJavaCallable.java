package com.github.chengyuxing.plugin.rabbit.sql.action;

import com.github.chengyuxing.plugin.rabbit.sql.common.Store;
import com.intellij.codeInsight.daemon.RelatedItemLineMarkerInfo;
import com.intellij.codeInsight.daemon.RelatedItemLineMarkerProvider;
import com.intellij.codeInsight.navigation.NavigationGutterIconBuilder;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.psi.PsiComment;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;
import java.util.stream.Collectors;


public class GotoJavaCallable extends RelatedItemLineMarkerProvider {

    @Override
    protected void collectNavigationMarkers(@NotNull PsiElement element, @NotNull Collection<? super RelatedItemLineMarkerInfo<?>> result) {
        if (!(element instanceof PsiComment)) {
            return;
        }
        String sqlNameTag = element.getText();
        if (sqlNameTag == null) {
            return;
        }
        var pattern = Pattern.compile("/\\*\\s*\\[\\s*(?<name>\\S+)\\s*]\\s*\\*/");
        var m = pattern.matcher(sqlNameTag);
        if (m.find()) {
            var sqlName = m.group("name");
            var xqlFile = element.getContainingFile();
            if (xqlFile != null) {
                var alias = xqlFile.getVirtualFile().getNameWithoutExtension();
                String sqlPath = alias + "." + sqlName;
                if (Store.INSTANCE.xqlFileManager.contains(sqlPath)) {
                    final var sqlRef = "&" + sqlPath;
                    var project = element.getProject();

                    List<PsiFile> founded = Store.INSTANCE.projectJavas.stream()
                            .map(p -> VirtualFileManager.getInstance().findFileByNioPath(p))
                            .filter(Objects::nonNull)
                            .map(vf -> PsiManager.getInstance(project).findFile(vf))
                            .filter(Objects::nonNull)
                            .filter(psi -> psi.getText().contains(sqlRef))
                            .collect(Collectors.toList());

                    if (!founded.isEmpty()) {
                        var markInfo = NavigationGutterIconBuilder.create(AllIcons.Actions.IntentionBulb)
                                .setTargets(founded)
                                .setTooltipText("Where I am!")
                                .createLineMarkerInfo(element);
                        result.add(markInfo);
                    }
                }
            }
        }
    }
}
