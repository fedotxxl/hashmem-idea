/*
 * HmLog
 * Copyright (c) 2012 Cybervision. All rights reserved.
 */
package com.hashmem.idea.ui;

import com.hashmem.idea.remote.SyncService;
import com.hashmem.idea.tracked.TrackedRunnable;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationGroup;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;

import javax.swing.*;

public class HmLog {

    private static final String TITLE = "hashMem.com plugin";

    public void noteNoteFound(String key) {
        noteNoteFound(key, true);
    }

    public void noteNoteFound(String key, boolean isInfo) {
        String message = "Note with key '" + key + "' is not found";

        if (isInfo) {
            info(message);
        } else {
            warn(message);
        }
    }

    public void canNotCreateFile(String key) {
        warn("Can't create note file with key '" + key + "'");
    }

    public void fileDeleted(String key) {
        info("Deleted note: " + key);
    }
    public void canNotDeletedFile(String key) {
        info("Can't delete note file with '" + key + "'");
    }

    public void incorrectPageUrl(String url) {
        warn("Incorrect page url: '" + url + "'");
    }

    public void unknownCommand(String key) {
        warn("Unknown command: '" + key + "'");
    }

    public void unableToSyncSinceItIsDisabled() {
        warn("Syncing with hashMem.com is disabled. <br>" +
                "Please use :settings command to specify username and password");
    }

    public void failedToSyncIncorrectUsernameOrPassword() {
        warn("Failed to sync notes with hashMem.com. Is username/password correct?");
    }

    public void failedToSyncUnknownResponse(int statusCode) {
        warn("Unknown hashMem.com notes sync response: " + statusCode);
    }

    public void failedToSyncIoException() {
        warn("Unable to connect to hashMem.com");
    }

    public void failedToSyncUnknownException() {
        warn("Unknown hashMem notes sync exception");
    }

    public void syncResult(SyncService.SyncResult result) {
        String message = "Successfully synced notes. <br>Sent: " + result.getSentToServerCount();
        if (result.hasCreated()) message += "; created: " + result.getCreated();
        if (result.hasUpdated()) message += "; updated: " + result.getUpdated();
        if (result.hasDeleted()) message += "; deleted: " + result.getDeleted();

        if (!result.hasCreated() && !result.hasUpdated() && !result.hasDeleted()) {
            message += "; received: 0";
        }

        info(message);
    }

    private void warn(String message) {
        notify(message, NotificationType.WARNING);
    }
    private void info(String message) {
        notify(message, NotificationType.INFORMATION);
    }

    private static void notify(String message, NotificationType notificationType) {
        final Notification notification = NotificationGroup.balloonGroup(TITLE).createNotification(message, notificationType);
        SwingUtilities.invokeLater(new TrackedRunnable() {
            @Override
            public void doRun() {
                Notifications.Bus.notify(notification);
            }
        });

    }
}
