/*
 * SyncService
 * Copyright (c) 2012 Cybervision. All rights reserved.
 */
package com.hashmem.idea.remote;

import com.google.common.collect.Lists;
import com.google.common.eventbus.Subscribe;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.hashmem.idea.domain.Note;
import com.hashmem.idea.domain.SyncNote;
import com.hashmem.idea.event.NoteFileChangedEvent;
import com.hashmem.idea.event.NoteFileDeletedEvent;
import com.hashmem.idea.service.NotesService;
import com.hashmem.idea.service.Router;
import com.hashmem.idea.service.SettingsService;
import com.hashmem.idea.ui.HmLog;
import com.hashmem.idea.utils.Callback;
import com.hashmem.idea.utils.Debouncer;
import com.hashmem.idea.utils.OneTimeContainer;
import com.hashmem.idea.tracked.TrackedRunnable;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.util.Condition;
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

import static com.intellij.util.containers.ContainerUtil.filter;
import static com.intellij.util.containers.ContainerUtil.map;

public class SyncService {

    private SettingsService settingsService;
    private AuthService authService;
    private HmLog log;
    private HttpService httpService;
    private NotesService notesService;
    private SyncChangeService syncChangeService;
    private Router router;
    private volatile boolean syncing = false;
    private long lastSync = 0l;
    private OneTimeContainer<String> notesToSkipChangeOrDeletedEvents = new OneTimeContainer<String>();
    private Debouncer<Object> syncOnChangeDebouncer = new Debouncer<Object>(5000, new Callback<Object>() {
        @Override
        public void call(Object arg) {
            doSync(false);
        }
    });

    private static final Condition NOT_NULL_CONDITION = new Condition<Object>() {
        @Override
        public boolean value(Object o) {
            return o != null;
        }
    };
    private static final Condition DELETED_ONLY_CONDITION = new Condition<SyncNote>() {
        @Override
        public boolean value(SyncNote o) {
            return o.isDeleted();
        }
    };

    private synchronized void doSync(boolean isForceSync) {
        if (syncing || !settingsService.isSyncEnabled()) {

            if (isForceSync) {
                log.unableToSyncSinceItIsDisabled();
            }

            return;
        }

        final long since = (isForceSync) ? 0l : lastSync;

        ApplicationManager.getApplication().invokeLater(new TrackedRunnable() {
            @Override
            public void doRun() {
                try {
                    syncing = true;

                    long synced = System.currentTimeMillis();
                    Collection<SyncNote> notesToServer = getNotesToSync(since);
                    Collection<SyncNote> notesFromServer = pushNotes(since, notesToServer);
                    saveChangedNotes(notesFromServer, synced, notesToServer);
                    lastSync = synced;
                } catch (NotAuthenticatedException nae) {
                    log.failedToSyncIncorrectUsernameOrPassword();
                } catch (UnknownSyncException use) {
                    log.failedToSyncUnknownResponse(use.getStatusCode());
                } catch (IOException io) {
                    log.failedToSyncIoException();
                } catch (Exception e) {
                    e.printStackTrace();
                    log.failedToSyncUnknownException();
                } finally {
                    syncing = false;
                }
            }
        });
    }

    public void syncAllNow() {
        syncBackground(new TrackedRunnable() {
            @Override
            public void doRun() {
                doSync(true);
            }
        });
    }

    public void syncOnChange() {
        syncBackground(new TrackedRunnable() {
            @Override
            public void doRun() {
                syncOnChangeDebouncer.call(Boolean.TRUE);
            }
        });
    }

    private void syncBackground(final TrackedRunnable runnable) {
        ProgressManager.getInstance().run(new Task.Backgroundable(null, "Syncing hashMem.com notes") {
            public void run(@NotNull ProgressIndicator progressIndicator) {
                progressIndicator.setFraction(0.10);

                runnable.run();

                progressIndicator.setFraction(1.0);
            }
        });
    }

    public void resetSyncData() {
        syncChangeService.forgetAll();
    }

    private void saveChangedNotes(final Collection<SyncNote> notes, final long synced, final Collection<SyncNote> notesToServer) {
        final SyncResult result = new SyncResult(notesToServer.size());

        syncChangeService.forget(filter(notesToServer, DELETED_ONLY_CONDITION));

        if (notes == null || notes.size() == 0) {
            log.syncResult(result);
        } else {
            ApplicationManager.getApplication().runWriteAction(new Runnable() {
                @Override
                public void run() {
                    for (SyncNote note : notes) {
                        SyncChangeResult change = saveNoteFromServer(note, synced);
                        result.increase(change);
                    }

                    log.syncResult(result);
                }
            });
        }
    }

    private SyncChangeResult saveNoteFromServer(SyncNote note, long synced) {
        SyncChangeResult answer;
        String key = note.getKey();

        if (note.isDeleted()) {
            if (notesService.has(key)) {
                answer = SyncChangeResult.DELETED;
            } else {
                answer = SyncChangeResult.NOTHING_CHANGED;
            }
        } else {
            Note localNote = notesService.getNote(key);

            if (localNote == null) {
                answer = SyncChangeResult.CREATED;
            } else if (!localNote.getContent().equals(note.getContent())) {
                answer = SyncChangeResult.UPDATED;
            } else {
                answer = SyncChangeResult.NOTHING_CHANGED;
            }
        }

        if (answer == SyncChangeResult.DELETED) {
            skipNextChangeOrDeletedEvent(key);
            syncChangeService.forget(key);
            notesService.remove(key);
        } else if (answer == SyncChangeResult.CREATED || answer == SyncChangeResult.UPDATED) {
            skipNextChangeOrDeletedEvent(key);
            syncChangeService.markAsUpdated(key, note.getLastUpdated());
            notesService.save(note);
        }

        return answer;
    }

    private Collection<SyncNote> getNotesToSync(long since) {
        Collection<SyncNote> answer = Lists.newArrayList();

        answer.addAll(filter(map(syncChangeService.getUpdatedSince(since), new Function<String, SyncNote>() {
            @Override
            public SyncNote fun(String key) {
                return getNoteToSync(key, syncChangeService.getLastUpdated(key), false);
            }
        }), NOT_NULL_CONDITION));
        answer.addAll(filter(map(syncChangeService.getDeletedSince(since), new Function<String, SyncNote>() {
            @Override
            public SyncNote fun(String key) {
                return getNoteToSync(key, syncChangeService.getLastUpdated(key), true);
            }
        }), NOT_NULL_CONDITION));

        return answer;
    }

    private Collection<SyncNote> pushNotes(long lastSync, final Collection<SyncNote> notesToServer) throws NotAuthenticatedException, IOException, UnknownSyncException {
        long now = System.currentTimeMillis();
        Map<String, Object> data = new HashMap<String, Object>();
        data.put("now", now);
        data.put("synced", lastSync);
        data.put("notes", notesToServer);

        HttpResponse response = doPushNotes(authService.getToken(), data);
        if (response.getStatusLine().getStatusCode() == 401) response = doPushNotes(authService.refreshToken(), data);

        Integer status = response.getStatusLine().getStatusCode();

        if (status == 200) {
            Type collectionType = new TypeToken<List<SyncNote>>(){}.getType();
            List<SyncNote> notesFromServer = new Gson().fromJson(IOUtils.toString(response.getEntity().getContent(), "UTF-8"), collectionType);
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

    private SyncNote getNoteToSync(String key, long lastUpdated, boolean isDeleted) {
        SyncNote answer = null;

        if (isDeleted) {
            return SyncNote.newDeletedNote(key, lastUpdated);
        } else {
            Note note = notesService.getNote(key);
            if (note == null) {
                //todo
            } else {
                answer = new SyncNote(note, lastUpdated);
            }
        }

        return answer;
    }

    private void skipNextChangeOrDeletedEvent(String key) {
        notesToSkipChangeOrDeletedEvents.put(key);
    }

    private void doOrSkipEventOnce(String key, Runnable runnable) {
        notesToSkipChangeOrDeletedEvents.checkAndDo(key, false, runnable);
    }

    @Subscribe
    public void onNoteFileDeleted(final NoteFileDeletedEvent e) {
        doOrSkipEventOnce(e.getKey(), new Runnable() {
            @Override
            public void run() {
                syncChangeService.markAsDeleted(e.getKey(), System.currentTimeMillis());
                syncOnChange();
            }
        });
    }

    @Subscribe
    public void onNoteFileChanged(final NoteFileChangedEvent e) {
        doOrSkipEventOnce(e.getKey(), new Runnable() {
            @Override
            public void run() {
                syncChangeService.markAsUpdated(e.getKey(), System.currentTimeMillis());
                syncOnChange();
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

    private static enum SyncChangeResult {
        CREATED, UPDATED, DELETED, NOTHING_CHANGED
    }

    public static class SyncResult {

        private int sentToServerCount = 0;
        private int created = 0;
        private int updated = 0;
        private int deleted = 0;

        private SyncResult(int sentToServerCount) {
            this.sentToServerCount = sentToServerCount;
        }

        public void increase(SyncChangeResult change) {
            if (change == SyncChangeResult.CREATED) {
                created++;
            } else if (change == SyncChangeResult.UPDATED) {
                updated++;
            } else if (change ==SyncChangeResult.DELETED) {
                deleted++;
            }
        }

        public int getSentToServerCount() {
            return sentToServerCount;
        }

        public int getCreated() {
            return created;
        }

        public int getUpdated() {
            return updated;
        }

        public int getDeleted() {
            return deleted;
        }

        public boolean hasCreated() {
            return created > 0;
        }

        public boolean hasUpdated() {
            return updated > 0;
        }

        public boolean hasDeleted() {
            return deleted > 0;
        }
    }

    private static class SyncParams {
        private long since;
        private boolean isForceSync;

        private SyncParams(long since, boolean isForceSync) {
            this.since = since;
            this.isForceSync = isForceSync;
        }

        public long getSince() {
            return since;
        }

        public boolean isForceSync() {
            return isForceSync;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            SyncParams that = (SyncParams) o;

            if (isForceSync != that.isForceSync) return false;
            if (since != that.since) return false;

            return true;
        }

        @Override
        public int hashCode() {
            int result = (int) (since ^ (since >>> 32));
            result = 31 * result + (isForceSync ? 1 : 0);
            return result;
        }
    }

    //=========== SETTERS ============
    public void setAuthService(AuthService authService) {
        this.authService = authService;
    }

    public void setLog(HmLog log) {
        this.log = log;
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

    public void setSettingsService(SettingsService settingsService) {
        this.settingsService = settingsService;
    }

}
