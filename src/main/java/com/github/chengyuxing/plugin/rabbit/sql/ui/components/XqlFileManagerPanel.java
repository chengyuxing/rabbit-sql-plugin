package com.github.chengyuxing.plugin.rabbit.sql.ui.components;

import com.github.chengyuxing.common.tuple.Quadruple;
import com.github.chengyuxing.common.tuple.Tuples;
import com.github.chengyuxing.common.utils.StringUtil;
import com.github.chengyuxing.plugin.rabbit.sql.actions.toolwindow.popup.*;
import com.github.chengyuxing.plugin.rabbit.sql.ui.renderer.TreeNodeRenderer;
import com.github.chengyuxing.plugin.rabbit.sql.ui.types.XqlTreeNodeData;
import com.github.chengyuxing.plugin.rabbit.sql.common.XQLConfigManager;
import com.github.chengyuxing.plugin.rabbit.sql.ui.types.XqlTreeNode;
import com.github.chengyuxing.plugin.rabbit.sql.util.*;
import com.github.chengyuxing.sql.XQLFileManager;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.actionSystem.*;
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

import static com.github.chengyuxing.common.utils.StringUtil.NEW_LINE;

public class XqlFileManagerPanel extends SimpleToolWindowPanel {
    private final Project project;
    private final ActionManager actionManager = ActionManager.getInstance();
    private final XQLConfigManager xqlConfigManager = XQLConfigManager.getInstance();

    private ActionPopupMenu xqlFileManagerMenu;
    private ActionPopupMenu xqlFragmentMenu;
    private ActionPopupMenu xqlFileMenu;
    private ActionPopupMenu xqlFolderMenu;
    private ActionPopupMenu moduleMenu;

    private Tree tree;
    private final Map<TreePath, Boolean> treeExpandedState = new HashMap<>();
    private boolean treeViewNodes = false;


    public XqlFileManagerPanel(boolean vertical, Project project) {
        super(vertical, true);
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
                    if (node.getUserObject() instanceof XqlTreeNodeData nodeSource) {
                        if (Objects.requireNonNull(nodeSource.type()) == XqlTreeNodeData.Type.XQL_FRAGMENT) {
                            var point = pointRef.get();
                            if (Objects.nonNull(point)) {
                                @SuppressWarnings("unchecked") var sqlMeta = (Quadruple<String, String, XQLFileManager.Sql, XQLConfigManager.Config>) nodeSource.source();
                                var sql = sqlMeta.getItem3();
                                var html = HtmlUtil.highlightSql(sql.getContent());
                                if (!sql.getDescription().isEmpty()) {
                                    var desc = HtmlUtil.pre("/*" + sql.getDescription() + "*/", HtmlUtil.Color.ANNOTATION);
                                    html = HtmlUtil.wrap("div", desc + html, HtmlUtil.Color.EMPTY);
                                }
                                var height = StringUtil.countOfContains(sql.getContent(), NEW_LINE) * 21 + 39;
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
                    if (node.getUserObject() instanceof XqlTreeNodeData nodeSource) {
                        switch (nodeSource.type()) {
                            case MODULE, XQL_CONFIG, XQL_FILE, XQL_FILE_FOLDER -> {
                            }
                            case XQL_FRAGMENT -> {
                                @SuppressWarnings("unchecked")
                                var sqlMeta = (Quadruple<String, String, XQLFileManager.Sql, XQLConfigManager.Config>) nodeSource.source();
                                var sqlPath = sqlMeta.getItem4().getXqlFileManager().getResource(sqlMeta.getItem1()).getFilename();
                                if (ProjectFileUtil.isLocalFileUri(sqlPath)) {
                                    PsiUtil.navigate2xqlFile(sqlMeta.getItem1(), sqlMeta.getItem2(), sqlMeta.getItem4());
                                } else {
                                    NotificationUtil.showMessage(project, "only support local file", NotificationType.WARNING);
                                }
                            }
                        }
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
                    if (node.getUserObject() instanceof XqlTreeNodeData nodeSource) {
                        switch (nodeSource.type()) {
                            case MODULE -> moduleMenu.getComponent().show(tree, e.getX(), e.getY());
                            case XQL_CONFIG -> xqlFileManagerMenu.getComponent().show(tree, e.getX(), e.getY());
                            case XQL_FILE -> xqlFileMenu.getComponent().show(tree, e.getX(), e.getY());
                            case XQL_FRAGMENT -> xqlFragmentMenu.getComponent().show(tree, e.getX(), e.getY());
                            case XQL_FILE_FOLDER -> xqlFolderMenu.getComponent().show(tree, e.getX(), e.getY());
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
        var scrollPane = createTreeSpeedSearchPane();

        setContent(scrollPane);
    }

    private @NotNull JBScrollPane createTreeSpeedSearchPane() {
        TreeUIHelper.getInstance().installTreeSpeedSearch(tree);
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
        xqlConfigManager.getConfigMap(project)
                .forEach((module, configs) -> {
                    var mNode = new XqlTreeNode(new XqlTreeNodeData(XqlTreeNodeData.Type.MODULE, module.getFileName().toString(), module));
                    configs.forEach(config -> {
                        if (config.isValid()) {
                            var ds = new XqlTreeNodeData(XqlTreeNodeData.Type.XQL_CONFIG, config.getConfigName(), config);
                            var configNode = new XqlTreeNode(ds);
                            mNode.add(configNode);
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
                                        var fileNode = new XqlTreeNode(new XqlTreeNodeData(XqlTreeNodeData.Type.XQL_FILE, alias, Tuples.of(alias, filename, resource.getFilename(), config, resource.getDescription())));
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
                e.getPresentation().setText("Copy Path/Reference...");
            }
        };
        return actionManager.createActionPopupMenu(ActionPlaces.POPUP, new ActionGroup() {
            @Override
            public AnAction @NotNull [] getChildren(@Nullable AnActionEvent e) {
                return new AnAction[]{
                        new NewSQLAction(tree),
                        new Separator(),
                        new CopyXqlFile(tree),
                        copyGroup,
                        new OpenInEditorAction(tree)
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

    private ActionPopupMenu createXqlFileManagerPopMenu(JTree tree) {

        return actionManager.createActionPopupMenu(ActionPlaces.POPUP, new ActionGroup() {
            @Override
            public AnAction @NotNull [] getChildren(@Nullable AnActionEvent e) {
                return new AnAction[]{
                        new NewXqlFileAction(tree),
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
        tree.getEmptyText().setText("Cannot find Maven resources root folder.");
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
