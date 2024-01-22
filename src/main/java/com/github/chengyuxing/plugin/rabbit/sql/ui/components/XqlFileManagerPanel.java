package com.github.chengyuxing.plugin.rabbit.sql.ui.components;

import com.github.chengyuxing.common.tuple.Tuples;
import com.github.chengyuxing.plugin.rabbit.sql.actions.toolwindow.popup.*;
import com.github.chengyuxing.plugin.rabbit.sql.ui.types.TreeNodeSource;
import com.github.chengyuxing.plugin.rabbit.sql.common.XQLConfigManager;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.SimpleToolWindowPanel;
import com.intellij.ui.TreeSpeedSearch;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.treeStructure.Tree;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class XqlFileManagerPanel extends SimpleToolWindowPanel {
    private final Project project;
    private final ActionManager actionManager = ActionManager.getInstance();
    private final XQLConfigManager xqlConfigManager = XQLConfigManager.getInstance();

    private Tree tree;
    private final Map<TreePath, Boolean> treeExpandedState = new HashMap<>();
    private ActionPopupMenu xqlFileManagerMenu;
    private ActionPopupMenu xqlFragmentMenu;
    private ActionPopupMenu xqlFileMenu;

    public XqlFileManagerPanel(boolean vertical, Project project) {
        super(vertical);
        this.project = project;
        setBorder(BorderFactory.createEmptyBorder());
        initToolbar();
        initContent();
        updateStates();
    }

    void initToolbar() {
        var group = new ActionGroup() {
            @Override
            public AnAction @NotNull [] getChildren(@Nullable AnActionEvent e) {
                return new AnAction[]{
                        actionManager.getAction("com.github.chengyuxing.plugin.rabbit.sql.actions.ReloadConfigAction"),
                        actionManager.getAction("xqlFileManager.toolwindow.Separator"),
                        actionManager.getAction("com.github.chengyuxing.plugin.rabbit.sql.actions.toolwindow.RefreshContentAction"),
                        actionManager.getAction("com.github.chengyuxing.plugin.rabbit.sql.actions.toolwindow.ExpandAllAction"),
                        actionManager.getAction("com.github.chengyuxing.plugin.rabbit.sql.actions.toolwindow.CollapseAllAction")
                };
            }
        };
        var toolbar = actionManager.createActionToolbar(ActionPlaces.TOOLBAR, group, true);
        toolbar.setShowSeparatorTitles(true);
        toolbar.setTargetComponent(getToolbar());
        setToolbar(toolbar.getComponent());
    }

    void initContent() {
        tree = createTree();
        xqlFileMenu = createXqlFilePopMenu(tree);
        xqlFileManagerMenu = createXqlFileManagerPopMenu(tree);
        xqlFragmentMenu = createXqlFragmentPopMenu(tree);

        tree.addMouseListener(new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent e) {

            }

            @Override
            public void mousePressed(MouseEvent e) {
                if (tree.getPathForLocation(e.getX(), e.getY()) == null)
                    return;
                tree.setSelectionPath(tree.getPathForLocation(e.getX(), e.getY()));
                if (e.getButton() == MouseEvent.BUTTON3) {
                    var selected = tree.getSelectionPath();
                    if (Objects.isNull(selected)) {
                        return;
                    }
                    var node = (XqlTreeNode) selected.getLastPathComponent();
                    if (node.getUserObject() instanceof TreeNodeSource nodeSource) {
                        switch (nodeSource.type()) {
                            case MODULE -> {
                            }
                            case XQL_CONFIG -> xqlFileManagerMenu.getComponent().show(tree, e.getX(), e.getY());
                            case XQL_FILE -> xqlFileMenu.getComponent().show(tree, e.getX(), e.getY());
                            case XQL_FRAGMENT -> xqlFragmentMenu.getComponent().show(tree, e.getX(), e.getY());
                        }
                    }
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {

            }

            @Override
            public void mouseEntered(MouseEvent e) {

            }

            @Override
            public void mouseExited(MouseEvent e) {

            }
        });
        var searchTree = new TreeSpeedSearch(tree);
        searchTree.setCanExpand(true);

        var scrollPane = new JBScrollPane(searchTree.getComponent());
        scrollPane.setBorder(BorderFactory.createEmptyBorder());

        setContent(scrollPane);
    }

    public void saveTreeExpandedState() {
        if (Objects.isNull(tree)) {
            return;
        }
        treeExpandedState.clear();
        var expandedPaths = tree.getExpandedDescendants(new TreePath(tree.getModel().getRoot()));
        while (Objects.nonNull(expandedPaths) && expandedPaths.hasMoreElements()) {
            var path = expandedPaths.nextElement();
            treeExpandedState.put(path, Boolean.TRUE);
        }
    }

    public void restoreTreeExpandedState() {
        if (Objects.isNull(tree)) {
            return;
        }
        for (var e : treeExpandedState.entrySet()) {
            var path = e.getKey();
            var expand = e.getValue();
            if (expand) {
                tree.expandPath(path);
            } else {
                tree.collapsePath(path);
            }
        }
    }

    public void updateStates() {
        if (Objects.isNull(tree)) {
            return;
        }
        var model = (DefaultTreeModel) tree.getModel();
        var root = (XqlTreeNode) model.getRoot();
        saveTreeExpandedState();
        root.removeAllChildren();
        xqlConfigManager.getConfigMap(project)
                .forEach((module, configs) -> {
                    var mNode = new XqlTreeNode(new TreeNodeSource(TreeNodeSource.NodeSourceType.MODULE, module.getFileName().toString(), module.toString()));
                    configs.forEach(config -> {
                        var ds = new TreeNodeSource(TreeNodeSource.NodeSourceType.XQL_CONFIG, config.getConfigName(), config);
                        var configNode = new XqlTreeNode(ds);
                        mNode.add(configNode);
                        config.getXqlFileManagerConfig().getFiles().forEach((alias, filename) -> {
                            var resource = config.getXqlFileManager().getResource(alias);
                            if (Objects.nonNull(resource)) {
                                var fileNode = new XqlTreeNode(new TreeNodeSource(TreeNodeSource.NodeSourceType.XQL_FILE, alias, Tuples.of(alias, filename, resource.getFilename())));
                                configNode.add(fileNode);
                                resource.getEntry().forEach((name, sql) -> {
                                    if (!name.startsWith("${") && !name.endsWith("}")) {
                                        var sqlNode = new XqlTreeNode(new TreeNodeSource(TreeNodeSource.NodeSourceType.XQL_FRAGMENT,
                                                name, Tuples.of(alias, name, sql, config)));
                                        fileNode.add(sqlNode);
                                    }
                                });
                            }
                        });
                    });
                    root.add(mNode);
                });
        model.reload();
        restoreTreeExpandedState();
    }

    public Tree getTree() {
        return tree;
    }

    private ActionPopupMenu createXqlFilePopMenu(JTree tree) {
        return actionManager.createActionPopupMenu(ActionPlaces.POPUP, new ActionGroup() {
            @Override
            public AnAction @NotNull [] getChildren(@Nullable AnActionEvent e) {
                return new AnAction[]{
                        new OpenXqlFileAction(tree)
                };
            }
        });
    }

    private ActionPopupMenu createXqlFileManagerPopMenu(JTree tree) {

        return actionManager.createActionPopupMenu(ActionPlaces.POPUP, new ActionGroup() {
            @Override
            public AnAction @NotNull [] getChildren(@Nullable AnActionEvent e) {
                return new AnAction[]{
                        new ToggleActiveAction(tree),
                        new ReloadSelectedAction(tree),
                        new Separator(),
                        new NewXqlFileAction(tree)
                };
            }
        });
    }

    private ActionPopupMenu createXqlFragmentPopMenu(JTree tree) {
        var copyGroup = new SplitButtonAction(new ActionGroup() {
            @Override
            public AnAction @NotNull [] getChildren(@Nullable AnActionEvent e) {
                return new AnAction[]{
                        new CopySqlAction(tree, CopySqlAction.CopyType.SQL_NAME),
                        new CopySqlAction(tree, CopySqlAction.CopyType.SQL_PATH),
                        new CopySqlAction(tree, CopySqlAction.CopyType.SQL_DEFINITION)
                };
            }
        }) {
            @Override
            public void update(@NotNull AnActionEvent e) {
                e.getPresentation().setText("Copy Name/Definition...");
            }
        };
        return actionManager.createActionPopupMenu(ActionPlaces.POPUP, new ActionGroup() {
            @Override
            public AnAction @NotNull [] getChildren(@Nullable AnActionEvent e) {
                return new AnAction[]{
                        new ExecuteSqlAction(tree),
                        new Separator(),
                        copyGroup,
                        new GotoXqlFileAction(tree)
                };
            }
        });
    }

    private Tree createTree() {
        var rootNode = new XqlTreeNode(project.getName());
        var model = new DefaultTreeModel(rootNode);
        var tree = new Tree(model);
        tree.expandPath(new TreePath(rootNode));
        tree.setRootVisible(false);
        tree.setCellRenderer(new TreeNodeRenderer());
        return tree;
    }

}
