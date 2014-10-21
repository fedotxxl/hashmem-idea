/*
 * TrackedDocumentAdapter
 * Copyright (c) 2012 Cybervision. All rights reserved.
 */
package com.hashmem.idea.tracked;

import com.hashmem.idea.utils.ExceptionTracker;
import com.intellij.ui.DocumentAdapter;

import javax.swing.event.DocumentEvent;

public abstract class TrackedDocumentAdapter extends DocumentAdapter {

    @Override
    protected void textChanged(DocumentEvent e) {
        try {
            doTextChanged(e);
        } catch (Throwable t) {
            ExceptionTracker.getInstance().trackAndRethrow(t);
        }
    }

    protected abstract void doTextChanged(DocumentEvent e);
}
