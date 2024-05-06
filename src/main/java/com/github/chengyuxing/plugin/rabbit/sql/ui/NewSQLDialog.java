package com.github.chengyuxing.plugin.rabbit.sql.ui;

import com.github.chengyuxing.common.utils.StringUtil;
import com.github.chengyuxing.plugin.rabbit.sql.ui.components.NewSQLForm;
import com.github.chengyuxing.plugin.rabbit.sql.util.HtmlUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.Map;

public class NewSQLDialog extends DialogWrapper {
    private final NewSQLForm newSQLForm;

    public NewSQLDialog(@Nullable Project project) {
        super(true);
        this.newSQLForm = new NewSQLForm();
        this.newSQLForm.setInputChanged(name -> {
            if (name.matches("[a-zA-Z][-\\w]+")) {
                setOKActionEnabled(true);
            } else {
                setOKActionEnabled(false);
                this.newSQLForm.setMessage(HtmlUtil.toHtml(HtmlUtil.span("'" + name + "' is invalid.", HtmlUtil.Color.WARNING)));
            }
        });
        setOKActionEnabled(false);
        setSize(450, 80);
        setTitle("New SQL");
        init();
    }

    @Override
    protected @Nullable JComponent createCenterPanel() {
        return newSQLForm;
    }

    @Override
    protected void doOKAction() {
        var data = newSQLForm.getData();
        var name = data.getItem1();
        var desc = data.getItem2();
        var tmp = """
                /*[${NAME}]*/
                /*#${DESC}#*/
                
                ;
                """;
        var sqlFragment = StringUtil.FMT.format(tmp, Map.of("NAME", name, "DESC", desc));
        System.out.println(sqlFragment);
    }
}
