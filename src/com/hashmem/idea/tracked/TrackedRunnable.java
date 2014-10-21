/*
 * TrackedRunnable
 * Copyright (c) 2012 Cybervision. All rights reserved.
 */
package com.hashmem.idea.tracked;

import com.hashmem.idea.utils.ExceptionTracker;

public abstract class TrackedRunnable implements Runnable {

    @Override
    public void run() {
        try {
            doRun();
        } catch (Throwable t) {
            ExceptionTracker.getInstance().trackAndRethrow(t);
        }
    }

    public abstract void doRun();

}
