/*
 * HashMemFileModel
 * Copyright (c) 2012 Cybervision. All rights reserved.
 */
package com.hashmem.jetbrains;

import com.intellij.ide.util.PsiElementListCellRenderer;
import com.intellij.ide.util.gotoByName.GotoFileCellRenderer;
import com.intellij.ide.util.gotoByName.GotoFileModel;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ex.WindowManagerEx;
import org.jetbrains.annotations.NotNull;

public class HashMemFileModel extends GotoFileModel {
    private final int myMaxSize;

    HashMemFileModel(@NotNull Project project) {
        super(project);
        myMaxSize = ApplicationManager.getApplication().isUnitTestMode() ? Integer.MAX_VALUE : WindowManagerEx.getInstanceEx().getFrame(project).getSize().width;
    }

    @Override
    public PsiElementListCellRenderer getListCellRenderer() {
        return new HashMemCellRenderer(myMaxSize);
    }
}
