package com.github.chengyuxing.plugin.rabbit.sql.plugins.database.extensions;

import com.github.chengyuxing.plugin.rabbit.sql.MessageBundle;
import com.github.chengyuxing.sql.XQLFileManager;
import com.intellij.codeInsight.daemon.RelatedItemLineMarkerInfo;
import com.intellij.codeInsight.daemon.RelatedItemLineMarkerProvider;
import com.intellij.codeInsight.navigation.NavigationGutterIconBuilder;
import com.intellij.icons.AllIcons;
import com.intellij.ide.util.DefaultPsiElementCellRenderer;
import com.intellij.openapi.diagnostic.ControlFlowException;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.psi.PsiComment;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.sql.psi.SqlParameter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;

public class XQLTemplateNavigator extends RelatedItemLineMarkerProvider {
    private final static Logger log = Logger.getInstance(XQLTemplateNavigator.class);

    @Override
    protected void collectNavigationMarkers(@NotNull PsiElement element, @NotNull Collection<? super RelatedItemLineMarkerInfo<?>> result) {
        if (element instanceof PsiComment comment) {
            handlerTemplateDeclareNavigate(comment, result);
            return;
        }
        if (element instanceof SqlParameter parameter) {
            handlerTemplateUsagesNavigate(parameter, result);
        }
    }

    private void handlerTemplateDeclareNavigate(PsiComment comment, Collection<? super RelatedItemLineMarkerInfo<?>> result) {
        String templateKey = detectTemplateKey(comment);
        if (templateKey == null) return;
        try {
            var targets = new HashSet<PsiElement>();
            var parameters = PsiTreeUtil.findChildrenOfType(comment.getContainingFile(), SqlParameter.class);
            for (PsiElement psiElement : parameters) {
                var holder = psiElement.getText();
                if (holder.startsWith("${") && holder.endsWith("}")) {
                    var paramName = holder.substring(2, holder.length() - 1).trim();
                    if (Objects.equals(paramName, templateKey)) {
                        targets.add(psiElement);
                    }
                }
            }
            if (targets.isEmpty()) return;
            var markInfo = NavigationGutterIconBuilder.create(AllIcons.Nodes.Template)
                    .setTargets(targets)
                    .setCellRenderer(() -> new DefaultPsiElementCellRenderer() {
                        @Override
                        public String getContainerText(PsiElement element, String name) {
                            PsiElement sqlNameE = element;
                            while ((sqlNameE = PsiTreeUtil.prevLeaf(sqlNameE)) != null) {
                                if (sqlNameE instanceof PsiComment nameComment) {
                                    var m = XQLFileManager.KEY_PATTERN.matcher(nameComment.getText());
                                    if (m.matches()) {
                                        return nameComment.getText();
                                    }
                                }
                            }
                            return super.getContainerText(element, name);
                        }
                    })
                    .setPopupTitle(MessageBundle.message("marker.navigate.template.declare.popup", templateKey, targets.size()))
                    .setTooltipText(MessageBundle.message("marker.navigate.template.declare.tooltip"))
                    .createLineMarkerInfo(comment);
            result.add(markInfo);
        } catch (Throwable e) {
            if (e instanceof ControlFlowException) {
                throw e;
            }
            log.warn(e);
        }
    }

    private void handlerTemplateUsagesNavigate(SqlParameter parameter, Collection<? super RelatedItemLineMarkerInfo<?>> result) {
        try {
            PsiElement target = null;
            String text = parameter.getText();
            if (text.startsWith("${") && text.endsWith("}")) {
                var templateKey = text.substring(2, text.length() - 1);
                var comments = PsiTreeUtil.findChildrenOfType(parameter.getContainingFile(), PsiComment.class);
                for (PsiComment comment : comments) {
                    var key = detectTemplateKey(comment);
                    if (Objects.equals(key, templateKey)) {
                        target = comment;
                        break;
                    }
                }
            }
            if (target == null) return;
            var markInfo = NavigationGutterIconBuilder.create(AllIcons.Nodes.Related)
                    .setTarget(target)
                    .setTooltipText(MessageBundle.message("marker.navigate.template.usage.tooltip"))
                    .createLineMarkerInfo(parameter);
            result.add(markInfo);
        } catch (Throwable e) {
            if (e instanceof ControlFlowException) {
                throw e;
            }
            log.warn(e);
        }
    }

    private static @Nullable String detectTemplateKey(PsiComment comment) {
        String templateKey = null;
        var declareTemplateM = XQLFileManager.KEY_PATTERN.matcher(comment.getText());
        if (declareTemplateM.matches()) {
            templateKey = declareTemplateM.group("partName");
        }
        if (templateKey == null) {
            declareTemplateM = XQLFileManager.INLINE_TEMPLATE_BEGIN_PATTERN.matcher(comment.getText());
            if (declareTemplateM.matches()) {
                templateKey = declareTemplateM.group("key");
            }
        }
        return templateKey;
    }
}
