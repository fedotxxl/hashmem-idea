/*
 * HashMemCommandPresentation
 * Copyright (c) 2012 Cybervision. All rights reserved.
 */
package com.hashmem.idea.ui;

import com.intellij.navigation.ItemPresentation;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class HmCommandPresentation implements ItemPresentation {

    public static HmCommandPresentation INSTANCE = new HmCommandPresentation();

    @Nullable
    @Override
    public String getPresentableText() {
        return null;
    }

    @Nullable
    @Override
    public String getLocationString() {
        return null;
    }

    @Nullable
    @Override
    public Icon getIcon(boolean unused) {
        return null;
    }
}
