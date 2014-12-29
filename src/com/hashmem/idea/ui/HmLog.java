/*
 * HmLog
 * Copyright (c) 2012 Cybervision. All rights reserved.
 */
package com.hashmem.idea.ui;

import com.hashmem.idea.domain.SyncResponse;
import com.hashmem.idea.i18n.MessageBundle;
import com.hashmem.idea.remote.SyncService;
import com.hashmem.idea.tracked.TrackedRunnable;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationGroup;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import org.apache.commons.lang.StringUtils;

import javax.swing.*;
import java.util.List;
import java.util.Map;

public class HmLog {

    private static final String TITLE = MessageBundle.message("log.title");

    public void noteNoteFound(String key) {
        noteNoteFound(key, true);
    }

    public void noteNoteFound(String key, boolean isInfo) {
        String message = MessageBundle.message("log.note.not_found", key);

        if (isInfo) {
            info(message);
        } else {
            warn(message);
        }
    }

    public void canNotCreateFile(String key) {
        warn(MessageBundle.message("log.note.can_not_create", key));
    }

    public void fileDeleted(String key) {
        info(MessageBundle.message("log.note.deleted", key));
    }

    public void canNotDeletedFile(String key) {
        info(MessageBundle.message("log.note.can_not_delete", key));
    }

    public void unknownCommand(String key) {
        warn(MessageBundle.message("log.command.unknown", key));
    }

    public void unableToSyncSinceItIsDisabled() {
        warn(MessageBundle.message("log.sync.failure.disabled"));
    }

    public void failedToSyncIncorrectUsernameOrPassword() {
        warn(MessageBundle.message("log.sync.failure.credentials"));
    }

    public void failedToSyncUnknownResponse(int statusCode) {
        warn(MessageBundle.message("log.sync.failure.incorrect_response_code", statusCode));
    }

    public void failedToSyncIoException() {
        warn(MessageBundle.message("log.sync.failure.connection"));
    }

    public void failedToSyncUnknownException() {
        warn(MessageBundle.message("log.sync.failure.exception"));
    }

    public void syncResult(SyncService.SyncResult result, SyncResponse syncResponse, boolean isForceSync) {
        if (isDoNotLogSyncResult(result, syncResponse, isForceSync)) return;

        String message = MessageBundle.message("log.sync.success.sent");

        if (!isForceSync) {
            if (result.hasChanged()) message += MessageBundle.message("log.sync.success.client.changed") + " " + result.getChanged();
            if (syncResponse.hasChanged()) message += MessageBundle.message("log.sync.success.server.changed") + " " + syncResponse.getChanged();
        }

        if (syncResponse.hasErrors()) {
            message += MessageBundle.message("log.sync.success.has_errors");

            for (Map.Entry<String, List<String>> e : syncResponse.getErrors().entrySet()) {
                message += "<br>" + getNoteSyncErrorTranslation(e.getKey()) + ": " + StringUtils.join(e.getValue(), ", ");
            }
        }

        if (syncResponse.hasErrors()) {
            warn(message);
        } else {
            info(message);
        }
    }

    private boolean isDoNotLogSyncResult(SyncService.SyncResult result, SyncResponse syncResponse, boolean isForceSync) {
        if (isForceSync) {
            return false;
        } else if (result.hasChanged() || syncResponse.hasChanged()) {
            return false;
        } else if (syncResponse.hasErrors()) {
            return false;
        } else {
            return true;
        }
    }

    private String getNoteSyncErrorTranslation(String key) {
        if ("key.invalid".equals(key)) {
            return MessageBundle.message("log.sync.success.error.invalid_key");
        } else if ("content.too-large".equals(key)) {
            return MessageBundle.message("log.sync.success.error.too_large");
        } else {
            return key;
        }
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
