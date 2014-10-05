/*
 * SyncService
 * Copyright (c) 2012 Cybervision. All rights reserved.
 */
package com.hashmem.idea.remote;

import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.hashmem.idea.*;
import com.hashmem.idea.ui.NotificationService;
import com.intellij.openapi.application.AccessToken;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SyncService {

    private SettingsService settingsService;
    private AuthService authService;
    private NotificationService notificationService;
    private HttpService httpService;
    private NotesService notesService;
    private Router router;
    private volatile boolean syncing = false;

    private long lastSync = 0l; //todo rememer

    public void setSettingsService(SettingsService settingsService) {
        this.settingsService = settingsService;
    }

    public synchronized void sync() {
        sync(getLastSync());
    }

    public synchronized void syncAll() {
        sync(0l);
    }

    private synchronized void sync(long since) {
        if (syncing || !settingsService.isSyncEnabled()) {
            return;
        }

        try {
            syncing = true;

            long synced = System.currentTimeMillis();
            Collection<Note> notes = pushNotes(since, getNotesToSync(since));
            saveChangedNotes(notes, synced);
            setLastSync(synced);
        } catch (NotAuthenticatedException nae) {
            notificationService.warn("Failed to sync hashMem notes. Is username/password correct?");
        } catch (UnknownSyncException use) {
            notificationService.warn("Unknown hashMem notes sync response: " + use.getStatusCode());
        } catch (Exception e) {
            e.printStackTrace();

            notificationService.warn("Unknown hashMem notes sync exception");
        } finally {
            syncing = false;
        }
    }

    public void syncLater() {
        ProgressManager.getInstance().run(new Task.Backgroundable(null, "Syncing hashMem.com notes") {
            public void run(@NotNull ProgressIndicator progressIndicator) {
                progressIndicator.setFraction(0.10);

                sync();

                progressIndicator.setFraction(1.0);
            }
        });
    }

    private void saveChangedNotes(Collection<Note> notes, long synced) {
        if (notes == null || notes.size() == 0) return;

        AccessToken token = null;

        try {
            token = ApplicationManager.getApplication().acquireWriteActionLock(SyncService.class);

            for (Note note : notes) {
                note.setLastUpdated(synced);
                notesService.save(note);
            }
        } finally {
            if (token != null) token.finish();
        }
    }

    private long getLastSync() {
        return lastSync;
    }

    private void setLastSync(long sync) {
        lastSync = sync;
    }

    private Collection<Note> getNotesToSync(long since) {
        Collection<Note> answer = Lists.newArrayList();

        answer.addAll(notesService.getNotesChangedSince(since));
        answer.addAll(notesService.getNotesDeletedSince(since));

        return answer;
    }

    private Collection<Note> pushNotes(long lastSync, Collection<Note> notesToServer) throws NotAuthenticatedException, IOException, UnknownSyncException {
        Map<String, Object> data = new HashMap<String, Object>();
        data.put("synced", lastSync);
        data.put("notes", notesToServer);

        HttpResponse response = doPushNotes(authService.getToken(), data);
        if (response.getStatusLine().getStatusCode() == 401) response = doPushNotes(authService.refreshToken(), data);


        Integer status = response.getStatusLine().getStatusCode();

        if (status == 200) {
            Type collectionType = new TypeToken<List<Note>>(){}.getType();
            List<Note> notesFromServer = new Gson().fromJson(IOUtils.toString(response.getEntity().getContent(), "UTF-8"), collectionType);
            return notesFromServer;
        } else if (status == 401) {
            throw new NotAuthenticatedException();
        } else {
            throw new UnknownSyncException(response);
        }
    }

    private HttpResponse doPushNotes(String token, Map data) throws IOException {
        return httpService.post(router.getSync(token), new Gson().toJson(data));
    }

    private static class UnknownSyncException extends Exception {

        private HttpResponse response;

        private UnknownSyncException(HttpResponse response) {
            this.response = response;
        }

        public HttpResponse getResponse() {
            return response;
        }

        public int getStatusCode() {
            return response.getStatusLine().getStatusCode();
        }
    }

    private static class SyncResponse {

        List<Note> data;

    }

    //=========== SETTERS ============
    public void setAuthService(AuthService authService) {
        this.authService = authService;
    }

    public void setNotificationService(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    public void setHttpService(HttpService httpService) {
        this.httpService = httpService;
    }

    public void setNotesService(NotesService notesService) {
        this.notesService = notesService;
    }

    public void setRouter(Router router) {
        this.router = router;
    }
}
