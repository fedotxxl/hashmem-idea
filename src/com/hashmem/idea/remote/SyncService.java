/*
 * SyncService
 * Copyright (c) 2012 Cybervision. All rights reserved.
 */
package com.hashmem.idea.remote;

import com.google.common.collect.Lists;
import com.google.common.eventbus.Subscribe;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.hashmem.NoteToSync;
import com.hashmem.idea.*;
import com.hashmem.idea.event.NoteFileChangedEvent;
import com.hashmem.idea.event.NoteFileDeletedEvent;
import com.hashmem.idea.ui.NotificationService;
import com.intellij.openapi.application.AccessToken;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.util.Function;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import static com.intellij.util.containers.ContainerUtil.map;

public class SyncService {

    private SettingsService settingsService;
    private AuthService authService;
    private NotificationService notificationService;
    private HttpService httpService;
    private NotesService notesService;
    private SyncChangeService syncChangeService;
    private Router router;
    private volatile boolean syncing = false;
    private ConcurrentMap<String, Boolean> notesToSkipChangeEvents = new ConcurrentHashMap<String, Boolean>();

    private long lastSync = 0l; //todo rememer

    public void setSettingsService(SettingsService settingsService) {
        this.settingsService = settingsService;
    }

    public synchronized void sync() {
        sync(lastSync);
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
            Collection<NoteToSync> notes = pushNotes(since, getNotesToSync(since));
            saveChangedNotes(notes, synced);
            lastSync = synced;
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

    private void markAsDeleted(String key) {
        syncChangeService.markAsDeleted(key);
    }

    private void markAsUpdated(String key, long date) {
        syncChangeService.markAsUpdated(key, date);
    }

    private void saveChangedNotes(Collection<NoteToSync> notes, long synced) {
        if (notes == null || notes.size() == 0) return;

        AccessToken token = null;

        try {
            token = ApplicationManager.getApplication().acquireWriteActionLock(SyncService.class);

            for (NoteToSync note : notes) {
                saveNoteFromServer(note, synced);
            }
        } finally {
            if (token != null) token.finish();
        }
    }

    private void saveNoteFromServer(NoteToSync note, long synced) {
        markAsUpdated(note.getKey(), synced);
        skipNextChangeEvent(note.getKey());
        notesService.save(note);
    }

    private Collection<NoteToSync> getNotesToSync(long since) {
        Collection<NoteToSync> answer = Lists.newArrayList();

        answer.addAll(map(syncChangeService.getUpdatedSince(since), new Function<String, NoteToSync>() {
            @Override
            public NoteToSync fun(String key) {
                return getNoteToSync(key, syncChangeService.getLastUpdated(key), false);
            }
        }));
        answer.addAll(map(syncChangeService.getDeletedSince(since), new Function<String, NoteToSync>() {
            @Override
            public NoteToSync fun(String key) {
                return getNoteToSync(key, syncChangeService.getLastUpdated(key), true);
            }
        }));

        return answer;
    }

    private Collection<NoteToSync> pushNotes(long lastSync, Collection<NoteToSync> notesToServer) throws NotAuthenticatedException, IOException, UnknownSyncException {
        long now = System.currentTimeMillis();
        Map<String, Object> data = new HashMap<String, Object>();
        data.put("now", now);
        data.put("synced", lastSync);
        data.put("notes", notesToServer);

        HttpResponse response = doPushNotes(authService.getToken(), data);
        if (response.getStatusLine().getStatusCode() == 401) response = doPushNotes(authService.refreshToken(), data);

        Integer status = response.getStatusLine().getStatusCode();

        if (status == 200) {
            Type collectionType = new TypeToken<List<NoteToSync>>(){}.getType();
            List<NoteToSync> notesFromServer = new Gson().fromJson(IOUtils.toString(response.getEntity().getContent(), "UTF-8"), collectionType);
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

    private NoteToSync getNoteToSync(String key, long lastUpdated, boolean isDeleted) {
        NoteToSync answer = null;

        if (isDeleted) {
            return NoteToSync.newDeletedNote(key, lastUpdated);
        } else {
            Note note = notesService.getNote(key);
            if (note == null) {
                //todo
            } else {
                answer = new NoteToSync(note, lastUpdated);
            }
        }

        return answer;
    }

    private void skipNextChangeEvent(String key) {
        notesToSkipChangeEvents.put(key, true);
    }

    private void doOrSkipChangeEventOnce(String key, Runnable runnable) {
        Boolean answer = notesToSkipChangeEvents.get(key);
        if (answer != null && answer.equals(true)) {
            notesToSkipChangeEvents.put(key, false);
        } else {
            runnable.run();
        }
    }

    @Subscribe
    public void onNoteFileDeleted(NoteFileDeletedEvent e) {
        markAsDeleted(e.getKey());
        syncLater();
    }

    @Subscribe
    public void onNoteFileChanged(final NoteFileChangedEvent e) {
        doOrSkipChangeEventOnce(e.getKey(), new Runnable() {
            @Override
            public void run() {
                markAsUpdated(e.getKey(), System.currentTimeMillis());
                syncLater();
            }
        });
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

    public void setSyncChangeService(SyncChangeService syncChangeService) {
        this.syncChangeService = syncChangeService;
    }
}
