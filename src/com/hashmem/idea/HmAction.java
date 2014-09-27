/*
 * HmAction
 * Copyright (c) 2012 Cybervision. All rights reserved.
 */
package com.hashmem.idea;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;

public class HmAction extends AnAction {
    @Override
    public void actionPerformed(AnActionEvent e) {
        showPopup(e.getProject(), null, null);
    }

    private void showPopup(Project project, HmModel model, HmCallback callback) {
        new HmPopup(project).show();

//        ListPopupStep step = new BaseListPopupStep<String>("asd", new String[] { "a", "b"});
//
//        JBPopupFactory.getInstance().createComponentPopupBuilder(new JTextField(), null).createPopup().showCenteredInCurrentWindow(project);
    }
}
