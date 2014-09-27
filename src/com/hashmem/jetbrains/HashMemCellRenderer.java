/*
 * HashMemCellRenderer
 * Copyright (c) 2012 Cybervision. All rights reserved.
 */
package com.hashmem.jetbrains;

import com.intellij.ide.util.gotoByName.GotoFileCellRenderer;
import com.intellij.psi.PsiFileSystemItem;

import javax.swing.*;

public class HashMemCellRenderer extends GotoFileCellRenderer {
    public HashMemCellRenderer(int maxSize) {
        super(maxSize);
    }

    @Override
    protected String getContainerText(PsiFileSystemItem element, String name) {
        return null;
    }

    @Override
    protected DefaultListCellRenderer getRightCellRenderer(final Object value) {
        return null;
    }
}
