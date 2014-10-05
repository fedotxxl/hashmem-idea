/*
 * SyncService
 * Copyright (c) 2012 Cybervision. All rights reserved.
 */
package com.hashmem.idea.remote;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.hashmem.idea.*;
import com.hashmem.idea.ui.NotificationService;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SyncService implements Startable {

    private SettingsService settingsService;
    private AuthService authService;
    private NotificationService notificationService;
    private HttpService httpService;
    private NotesService notesService;
    private Router router;

    private long lastSync = 0l; //todo rememer

    public void setSettingsService(SettingsService settingsService) {
        this.settingsService = settingsService;
    }

    @Override
    public void postConstruct() {
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    try {
                        Thread.sleep(10 * 1000);
                        Integer periodicity = settingsService.getSyncPeriodicityInSeconds();
                        boolean isNeverSynced = lastSync == 0l;

                        if (periodicity > 0 && (isNeverSynced || (System.currentTimeMillis() - lastSync)/1000 > periodicity)) {
                            sync();
                        }
                    } catch (InterruptedException e) {
                        //
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        t.setDaemon(true);
        t.start();
    }

    public synchronized void sync() {
        if (!settingsService.isSyncEnabled()) {
            return;
        }

        try {
            Collection<Note> notes = pushNotes(getLastSync(), getNotesToSync());
            long synced = System.currentTimeMillis();
            saveChangedNotes(notes, synced);
            setLastSync(synced);
        } catch (NotAuthenticatedException nae) {
            notificationService.warn("Failed to sync hashMem notes. Is username/password correct?");
        } catch (UnknownSyncException use) {
            notificationService.warn("Unknown hashMem notes sync response: " + use.getStatusCode());
        } catch (Exception e) {
            e.printStackTrace();

            notificationService.warn("Unknown hashMem notes sync exception");
        }
    }

    private void saveChangedNotes(Collection<Note> notes, long synced) {
        for (Note note : notes) {
            note.setLastUpdated(synced);
            notesService.save(note);
        }
    }

    private long getLastSync() {
        return lastSync;
    }

    private void setLastSync(long sync) {
        lastSync = sync;
    }

    private Collection<Note> getNotesToSync() {
        return null;
    }

    private Collection<Note> pushNotes(Long lastSync, Collection<Note> notesToServer) throws NotAuthenticatedException, IOException, UnknownSyncException {
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
