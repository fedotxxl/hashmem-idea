/*
 * TrackedActionListener
 * Copyright (c) 2012 Cybervision. All rights reserved.
 */
package com.hashmem.idea.tracked;

import com.hashmem.idea.utils.ExceptionTracker;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public abstract class TrackedActionListener implements ActionListener {
    @Override
    public void actionPerformed(ActionEvent e) {
        try {
            doActionPerformed(e);
        } catch (Throwable t) {
            ExceptionTracker.getInstance().trackAndRethrow(t);
        }
    }

    public abstract void doActionPerformed(ActionEvent e);
}
