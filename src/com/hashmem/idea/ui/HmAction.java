/*
 * HmAction
 * Copyright (c) 2012 Cybervision. All rights reserved.
 */
package com.hashmem.idea.ui;

import com.hashmem.idea.HashMemApplicationComponent;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;

public class HmAction extends AnAction {
    @Override
    public void actionPerformed(AnActionEvent e) {
        showPopup(e.getProject());
    }

    private void showPopup(Project project) {
        HashMemApplicationComponent component = ApplicationManager.getApplication().getComponent(HashMemApplicationComponent.class);
        new HmPopup(project, component.getNotesService(), component.getActionProcessor()).show();
    }
}
