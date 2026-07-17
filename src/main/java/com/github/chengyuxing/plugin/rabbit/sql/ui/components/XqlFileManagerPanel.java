package com.github.chengyuxing.plugin.rabbit.sql.ui.components;

import com.github.chengyuxing.common.script.pipe.BuiltinPipes;
import com.github.chengyuxing.common.util.StringUtils;
import com.github.chengyuxing.plugin.rabbit.sql.MessageBundle;
import com.github.chengyuxing.plugin.rabbit.sql.actions.toolwindow.popup.*;
import com.github.chengyuxing.plugin.rabbit.sql.ui.renderer.TreeNodeRenderer;
import com.github.chengyuxing.plugin.rabbit.sql.common.XQLConfigManager;
import com.github.chengyuxing.plugin.rabbit.sql.ui.types.tree.XqlTreeNode;
import com.github.chengyuxing.plugin.rabbit.sql.ui.types.tree.data.NodeData;
import com.github.chengyuxing.plugin.rabbit.sql.ui.types.tree.data.impl.*;
import com.github.chengyuxing.plugin.rabbit.sql.util.*;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.module.ModuleUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.SimpleToolWindowPanel;
import com.intellij.openapi.ui.popup.JBPopup;
import com.intellij.ui.TreeUIHelper;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.treeStructure.Tree;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.*;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static com.github.chengyuxing.common.util.StringUtils.NEW_LINE;


public class XqlFileManagerPanel extends SimpleToolWindowPanel {
    private final Project project;
    private final ActionManager actionManager = ActionManager.getInstance();
    private final XQLConfigManager xqlConfigManager;

    private ActionPopupMenu xqlFileManagerMenu;
    private ActionPopupMenu xqlFragmentMenu;
    private ActionPopupMenu xqlFileMenu;
    private ActionPopupMenu xqlFolderMenu;
    private ActionPopupMenu moduleMenu;
    private ActionPopupMenu pipeMenu;

    private Tree tree;
    private final Map<TreePath, Boolean> treeExpandedState = new HashMap<>();
    private boolean treeViewNodes = false;


    public XqlFileManagerPanel(boolean vertical, Project project) {
        super(vertical, true);
        this.project = project;
        this.xqlConfigManager = XQLConfigManager.getInstance(project);
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
                        actionManager.getAction("com.github.chengyuxing.plugin.rabbit.sql.actions.toolwindow.CollapseAllAction"),
                        actionManager.getAction("com.github.chengyuxing.plugin.rabbit.sql.actions.toolwindow.ToggleViewModeAction"),
                        actionManager.getAction("xqlFileManager.toolwindow.Separator"),
                        actionManager.getAction("com.github.chengyuxing.plugin.rabbit.sql.actions.StatisticsAction")
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
        moduleMenu = createModuleMenu(tree);
        xqlFolderMenu = createXqlFolderPopMenu(tree);
        pipeMenu = createPipePopMenu(tree);

        AtomicReference<Point> pointRef = new AtomicReference<>();
        tree.addKeyListener(new KeyAdapter() {
            private JBPopup popup;

            @Override
            public void keyPressed(KeyEvent e) {
                if (Objects.nonNull(popup) && !popup.isDisposed()) {
                    popup.dispose();
                    return;
                }
                if (e.getKeyCode() == KeyEvent.VK_SPACE) {
                    var selection = tree.getSelectionPath();
                    if (Objects.isNull(selection)) {
                        return;
                    }
                    var node = (XqlTreeNode) selection.getLastPathComponent();
                    if (node.getUserObject() instanceof NodeData source) {
                        var point = pointRef.get();
                        if (point == null) {
                            return;
                        }
                        if (source instanceof SqlFragment sqlFragment) {
                            var sql = sqlFragment.sql();
                            var html = HtmlUtil.highlightSql(sql.getSource());
                            if (!sql.getDescription().isEmpty()) {
                                var desc = HtmlUtil.pre("/*" + sql.getDescription() + "*/", HtmlUtil.Color.ANNOTATION);
                                html = HtmlUtil.wrap("div", desc + html, HtmlUtil.Color.EMPTY);
                            }
                            var height = StringUtils.countOccurrences(sql.getSource(), NEW_LINE) * 21 + 39;
                            popup = SwingUtil.showPreview(html, height, tree.getComponentAt(point), point);
                            return;
                        }
                        if (source instanceof XqlFile xqlFile) {
                            var config = xqlFile.config().getXqlFileManager();
                            var alias = xqlFile.alias();
                            var errors = config.getErrorAlias();
                            var error = errors.get(alias);
                            if (error != null) {
                                var html = HtmlUtil.wrap("pre", error, HtmlUtil.Color.ERROR);
                                var height = StringUtils.countOccurrences(html, NEW_LINE) * 21 + 39;
                                popup = SwingUtil.showPreview(html, height, tree.getComponentAt(point), point);
                            }
                        }
                    }
                }
            }
        });
        tree.addMouseListener(new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent e) {
                pointRef.set(e.getPoint());
                if (e.getButton() == MouseEvent.BUTTON1 && e.getClickCount() == 2) {
                    var selected = tree.getSelectionPath();
                    if (Objects.isNull(selected)) {
                        return;
                    }
                    var node = (XqlTreeNode) selected.getLastPathComponent();
                    if (node.getUserObject() instanceof SqlFragment sqlFragment) {
                        var sqlPath = sqlFragment.config().getXqlFileManager().getResource(sqlFragment.xqlAlias()).getFilename();
                        if (ProjectFileUtil.isLocalFileUri(sqlPath)) {
                            PsiUtil.navigate2xqlFile(sqlFragment.xqlAlias(), sqlFragment.sqlName(), sqlFragment.config());
                        } else {
                            NotificationUtil.showMessage(project, MessageBundle.message("ui.xqlFileManagerPanel.xql.parse.warning"), NotificationType.WARNING);
                        }
                        return;
                    }
                    if (node.getUserObject() instanceof PipeName pipeName) {
                        var module = ModuleUtil.findModuleForFile(pipeName.config().getConfigVfs(), project);
                        ProjectFileUtil.openJavaFile(project, module, pipeName.className());
                    }
                }
            }

            @Override
            public void mousePressed(MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON3) {
                    var selected = tree.getSelectionPath();
                    if (Objects.isNull(selected)) {
                        return;
                    }
                    var node = (XqlTreeNode) selected.getLastPathComponent();
                    Object source = node.getUserObject();
                    if (source instanceof ProjectModule) {
                        moduleMenu.getComponent().show(tree, e.getX(), e.getY());
                        return;
                    }
                    if (source instanceof XqlConfig) {
                        xqlFileManagerMenu.getComponent().show(tree, e.getX(), e.getY());
                        return;
                    }
                    if (source instanceof XqlFile) {
                        xqlFileMenu.getComponent().show(tree, e.getX(), e.getY());
                        return;
                    }
                    if (source instanceof SqlFragment) {
                        xqlFragmentMenu.getComponent().show(tree, e.getX(), e.getY());
                        return;
                    }
                    if (source instanceof XqlFileFolder) {
                        xqlFolderMenu.getComponent().show(tree, e.getX(), e.getY());
                        return;
                    }
                    if (source instanceof PipeName) {
                        pipeMenu.getComponent().show(tree, e.getX(), e.getY());
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
        var scrollPane = createTreeSpeedSearchPane();

        setContent(scrollPane);
    }

    private @NotNull JBScrollPane createTreeSpeedSearchPane() {
        TreeUIHelper.getInstance().installTreeSpeedSearch(tree, TreePath::toString, true);
        var scrollPane = new JBScrollPane(tree);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        return scrollPane;
    }

    void saveTreeExpandedState() {
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

    void restoreTreeExpandedState() {
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
        xqlConfigManager.getConfigMap()
                .forEach((module, configs) -> {
                    var mNode = new XqlTreeNode(new ProjectModule(module));
                    configs.forEach(config -> {
                        if (config.isValid()) {
                            var configNode = new XqlTreeNode(new XqlConfig(config));
                            mNode.add(configNode);

                            var pipeMaps = config.getXqlFileManagerConfig().getPipes();
                            var pipeFolderNode = new XqlTreeNode(new PipeFolder());
                            configNode.add(pipeFolderNode);
                            BuiltinPipes.getAll().forEach((k, c) -> {
                                var pipe = new XqlTreeNode(new PipeName(k, c.getClass().getName(), true, config));
                                pipeFolderNode.add(pipe);
                            });
                            pipeMaps.forEach((k, v) -> {
                                var pipe = new XqlTreeNode(new PipeName(k, v, false, config));
                                pipeFolderNode.add(pipe);
                            });

                            if (treeViewNodes) {
                                var nestTreeNodes = new LinkedHashMap<String, Object>();
                                config.getXqlFileManagerConfig().getFiles().forEach((alias, filename) -> {
                                    var isURI = ProjectFileUtil.isURI(filename);
                                    var paths = getPaths(alias, filename, isURI);
                                    SwingUtil.path2tree(paths, nestTreeNodes);
                                });
                                SwingUtil.buildXQLTree(nestTreeNodes, config, configNode);
                            } else {
                                config.getXqlFileManagerConfig().getFiles().forEach((alias, filename) -> {
                                    var resource = config.getXqlFileManager().getResource(alias);
                                    if (Objects.nonNull(resource)) {
                                        var fileNode = new XqlTreeNode(new XqlFile(alias, filename, resource, config));
                                        configNode.add(fileNode);
                                        SwingUtil.buildXQLNodes(config, alias, fileNode, resource);
                                    }
                                });
                            }
                        }
                    });
                    root.add(mNode);
                });
        model.reload();
        restoreTreeExpandedState();
    }

    private static @NotNull ArrayList<String> getPaths(String alias, String filename, boolean isURI) {
        String newFilename = filename;
        if (isURI) {
            // http://server/home.xql?id=1
            // server/home.xql
            int qIdx = newFilename.indexOf('?');
            if (qIdx > 0) {
                newFilename = newFilename.substring(0, qIdx);
            }
            int hashIdx = newFilename.indexOf('#');
            if (hashIdx > 0) {
                newFilename = newFilename.substring(0, hashIdx);
            }
            newFilename = newFilename.replaceAll("(?:file|http|https|ftp):/+(.+)", "$1");
        }
        int dIdx = newFilename.lastIndexOf("/");
        String aliasPath = dIdx != -1 ? newFilename.substring(0, dIdx + 1) + alias : alias;
        var paths = new ArrayList<>(List.of(aliasPath.split("/+")));
        if (isURI) {
            int colonIdx = filename.indexOf(':');
            paths.add(0, filename.substring(0, colonIdx + 3));
        }
        return paths;
    }

    public Tree getTree() {
        return tree;
    }

    private ActionPopupMenu createModuleMenu(JTree tree) {
        return actionManager.createActionPopupMenu(ActionPlaces.POPUP, new ActionGroup() {
            @Override
            public AnAction @NotNull [] getChildren(@Nullable AnActionEvent anActionEvent) {
                return new AnAction[]{
                        new NewXqlFileManagerAction(tree)
                };
            }
        });
    }

    private ActionPopupMenu createXqlFilePopMenu(JTree tree) {
        var copyGroup = new SplitButtonAction(new ActionGroup() {
            @Override
            public AnAction @NotNull [] getChildren(@Nullable AnActionEvent e) {
                return new AnAction[]{
                        new CopySqlAction(tree, CopySqlAction.CopyType.ALIAS),
                        new CopySqlAction(tree, CopySqlAction.CopyType.ABSOLUTE_PATH),
                        new CopySqlAction(tree, CopySqlAction.CopyType.PATH_FROM_CLASSPATH),
                        new CopySqlAction(tree, CopySqlAction.CopyType.YML_ARRAY_PATH_FROM_CLASSPATH),
                };
            }
        }) {
            @Override
            public void update(@NotNull AnActionEvent e) {
                e.getPresentation().setText(MessageBundle.message("ui.xqlFileManagerPanel.copyGroup.xql"));
            }
        };
        return actionManager.createActionPopupMenu(ActionPlaces.POPUP, new ActionGroup() {
            @Override
            public AnAction @NotNull [] getChildren(@Nullable AnActionEvent e) {
                return new AnAction[]{
                        new NewSQLAction(tree),
                        new Separator(),
                        new GenerateXqlMapperAction(tree),
                        new Separator(),
                        new CopyXqlFile(tree),
                        copyGroup,
                        new OpenInEditorAction(tree),
                        new Separator(),
                        new RemoveAction(tree),
                };
            }
        });
    }

    private ActionPopupMenu createXqlFolderPopMenu(Tree tree) {
        return actionManager.createActionPopupMenu(ActionPlaces.POPUP, new ActionGroup() {
            @Override
            public AnAction @NotNull [] getChildren(@Nullable AnActionEvent anActionEvent) {
                return new AnAction[]{
                        new NewXqlFileAction(tree)
                };
            }
        });
    }

    private ActionPopupMenu createPipePopMenu(Tree tree) {
        return actionManager.createActionPopupMenu(ActionPlaces.POPUP, new ActionGroup() {
            @Override
            public AnAction @NotNull [] getChildren(@Nullable AnActionEvent anActionEvent) {
                return new AnAction[]{
                        new OpenPipeDefAction(tree),
                        new Separator(),
                        new RemoveAction(tree)
                };
            }
        });
    }

    private ActionPopupMenu createXqlFileManagerPopMenu(JTree tree) {
        var configGroup = new SplitButtonAction(new ActionGroup() {
            @Override
            public AnAction @NotNull [] getChildren(@Nullable AnActionEvent e) {
                return new AnAction[]{
                        new NewXqlFileAction(tree),
                        new NewPipeAction(tree),
                };
            }
        }) {
            @Override
            public void update(@NotNull AnActionEvent e) {
                e.getPresentation().setText(MessageBundle.message("new.text"));
            }
        };

        return actionManager.createActionPopupMenu(ActionPlaces.POPUP, new ActionGroup() {
            @Override
            public AnAction @NotNull [] getChildren(@Nullable AnActionEvent e) {
                return new AnAction[]{
                        configGroup,
                        new OpenInEditorAction(tree),
                        new Separator(),
                        new ReloadSelectedAction(tree),
                        new ToggleActiveAction(tree)
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
                e.getPresentation().setText(MessageBundle.message("ui.xqlFileManagerPanel.copyGroup.sql"));
            }
        };
        return actionManager.createActionPopupMenu(ActionPlaces.POPUP, new ActionGroup() {
            @Override
            public AnAction @NotNull [] getChildren(@Nullable AnActionEvent e) {
                return new AnAction[]{
                        new ExecuteSqlAction(tree),
                        new Separator(),
                        new GenerateEntityAction(tree),
                        new Separator(),
                        copyGroup,
                        new GotoXqlFileAction(tree),
                        new Separator(),
                        new RemoveAction(tree),
                };
            }
        });
    }

    private Tree createTree() {
        var rootNode = new XqlTreeNode(project.getName());
        var model = new DefaultTreeModel(rootNode);
        var tree = new Tree(model);
        tree.getEmptyText().setText(MessageBundle.message("ui.xqlFileManagerPanel.empty"));
        tree.expandPath(new TreePath(rootNode));
        tree.setRootVisible(false);
        tree.setCellRenderer(new TreeNodeRenderer(() -> treeViewNodes));
        return tree;
    }

    public boolean isTreeViewNodes() {
        return treeViewNodes;
    }

    public void setTreeViewNodes(boolean treeViewNodes) {
        this.treeViewNodes = treeViewNodes;
    }
}
