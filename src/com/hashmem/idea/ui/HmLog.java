/*
 * HmLog
 * Copyright (c) 2012 Cybervision. All rights reserved.
 */
package com.hashmem.idea.ui;

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
        info("Note '" + key + "' was successfully deleted");
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

    private void warn(String message) {
        notify(message, NotificationType.WARNING);
    }
    private void info(String message) {
        notify(message, NotificationType.INFORMATION);
    }

    private static void notify(String message, NotificationType notificationType) {
        final Notification notification = NotificationGroup.balloonGroup(TITLE).createNotification(message, notificationType);
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                Notifications.Bus.notify(notification);
            }
        });

    }

}
