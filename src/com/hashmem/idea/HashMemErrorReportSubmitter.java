/*
 * HashMemErrorReportSubmitter
 * Copyright (c) 2012 Cybervision. All rights reserved.
 */
package com.hashmem.idea;

import com.hashmem.idea.utils.ExceptionTracker;
import com.intellij.openapi.diagnostic.ErrorReportSubmitter;
import com.intellij.openapi.diagnostic.IdeaLoggingEvent;
import com.intellij.openapi.diagnostic.SubmittedReportInfo;
import com.intellij.util.Consumer;

import java.awt.*;

public class HashMemErrorReportSubmitter extends ErrorReportSubmitter {
    @Override
    public String getReportActionText() {
        return "Report to hashMem.com plugin author (Thanks for your Help to improve the Plugin!)";
    }

    @Override
    public SubmittedReportInfo submit(IdeaLoggingEvent[] events, Component parentComponent) {
        // obsolete API -> see com.intellij.diagnostic.ITNReporter
        return new SubmittedReportInfo(null, "0", SubmittedReportInfo.SubmissionStatus.FAILED);
    }

    @Override
    public boolean trySubmitAsync(IdeaLoggingEvent[] events, String additionalInfo, Component parentComponent, Consumer<SubmittedReportInfo> consumer) {
        for (IdeaLoggingEvent event : events) {
            ExceptionTracker.getInstance().track(event.getThrowable());
        }

        return true;
    }
}
