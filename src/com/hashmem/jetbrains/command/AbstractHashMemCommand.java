/*
 * HashMemCommand
 * Copyright (c) 2012 Cybervision. All rights reserved.
 */
package com.hashmem.jetbrains.command;

import com.intellij.navigation.ItemPresentation;
import com.intellij.navigation.NavigationItem;
import org.jetbrains.annotations.Nullable;

abstract class AbstractHashMemCommand implements NavigationItem, HashMemCommand {

    @Nullable
    @Override
    public ItemPresentation getPresentation() {
        return HashMemCommandPresentation.INSTANCE;
    }

    @Override
    public void navigate(boolean requestFocus) {

    }

    @Override
    public boolean canNavigate() {
        return false;
    }

    @Override
    public boolean canNavigateToSource() {
        return false;
    }

    public String toString() {
        return getName();
    }
}
