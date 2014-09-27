/*
 * SyncHashMemCommand
 * Copyright (c) 2012 Cybervision. All rights reserved.
 */
package com.hashmem.jetbrains.command;

import org.jetbrains.annotations.Nullable;

public class SyncHashMemCommand extends AbstractHashMemCommand {

    public static SyncHashMemCommand INSTANCE = new SyncHashMemCommand();

    @Override
    public void process() {
        System.out.println("Do sync!");
    }

    @Nullable
    @Override
    public String getName() {
        return ":sync";
    }
}
