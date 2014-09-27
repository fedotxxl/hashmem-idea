/*
 * HashMemCommandPresentation
 * Copyright (c) 2012 Cybervision. All rights reserved.
 */
package com.hashmem.jetbrains.command;

import com.intellij.navigation.ItemPresentation;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class HashMemCommandPresentation implements ItemPresentation {

    public static HashMemCommandPresentation INSTANCE = new HashMemCommandPresentation();

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
