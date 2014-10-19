/*
 * SyncChangeService
 * Copyright (c) 2012 Cybervision. All rights reserved.
 */
package com.hashmem.idea.remote;

import com.google.gson.Gson;
import com.hashmem.NoteToSync;
import com.hashmem.idea.FileSystem;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.util.Computable;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Collection;
import java.util.List;

public class SyncChangeService {

    private FileSystem fileSystem;
    private SyncChangeData syncChangeData;

    void markAsDeleted(String key) {
        markAsDeleted(key, System.currentTimeMillis());
    }

    void markAsUpdated(String key) {
        markAsUpdated(key, System.currentTimeMillis());
    }

    void markAsDeleted(String key, long date) {
        getSyncChangeData().markAsDeleted(key, date);
        saveSyncChangeData();
    }

    void markAsUpdated(String key, long date) {
        getSyncChangeData().markAsUpdated(key, date);
        saveSyncChangeData();
    }

    public void forget(Collection<NoteToSync> notes) {
        SyncChangeData data = getSyncChangeData();

        for (NoteToSync note : notes) {
            data.forget(note.getKey());
        }

        saveSyncChangeData();
    }

    public void forgetAll() {
        getSyncChangeData().forgetAll();
        saveSyncChangeData();
    }

    List<String> getUpdatedSince(long since) {
        return getSyncChangeData().getUpdatedSince(since);
    }

    List<String> getDeletedSince(long since) {
       return getSyncChangeData().getDeletedSince(since);
    }

    Long getLastUpdated(String key) {
        return getSyncChangeData().getLastUpdated(key);
    }

    private void saveSyncChangeData() {
        try {
            fileSystem.getSyncFile().setBinaryContent(new Gson().toJson(syncChangeData).getBytes("UTF-8"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private SyncChangeData getSyncChangeData() {
        if (syncChangeData == null) {
            syncChangeData = ApplicationManager.getApplication().runReadAction(new Computable<SyncChangeData>() {
                @Override
                public SyncChangeData compute() {
                    try {
                        SyncChangeData answer = new Gson().fromJson(new InputStreamReader(fileSystem.getSyncFile().getInputStream()), SyncChangeData.class);

                        if (answer != null) {
                            return answer;
                        } else {
                            return new SyncChangeData();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                        return null;
                    }
                }
            });
        }

        return syncChangeData;
    }

    //=========== SETTERS ============
    public void setFileSystem(FileSystem fileSystem) {
        this.fileSystem = fileSystem;
    }
}
