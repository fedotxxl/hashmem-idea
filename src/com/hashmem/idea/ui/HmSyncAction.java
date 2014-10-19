/*
 * HmSyncAction
 * Copyright (c) 2012 Cybervision. All rights reserved.
 */
package com.hashmem.idea.ui;

import com.hashmem.idea.HashMemApplicationComponent;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.application.ApplicationManager;

public class HmSyncAction extends AnAction {

    @Override
    public void actionPerformed(AnActionEvent e) {
        ApplicationManager.getApplication().getComponent(HashMemApplicationComponent.class).getSyncService().syncAllNow();
    }
}
