/*
 * TrackedItemListener
 * Copyright (c) 2012 Cybervision. All rights reserved.
 */
package com.hashmem.idea.tracked;

import com.hashmem.idea.utils.ExceptionTracker;

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

public abstract class TrackedItemListener implements ItemListener {

    @Override
    public void itemStateChanged(ItemEvent e) {
        try {
            doItemStateChanged(e);
        } catch (Throwable t) {
            ExceptionTracker.getInstance().trackAndRethrow(t);
        }
    }

    public abstract void doItemStateChanged(ItemEvent e);
}
