/*
 * SyncChangeService
 * Copyright (c) 2012 Cybervision. All rights reserved.
 */
package com.hashmem.idea.remote;

import com.google.gson.Gson;
import com.hashmem.idea.domain.SyncNote;
import com.hashmem.idea.service.FileSystem;
import com.hashmem.idea.utils.Callback;
import com.hashmem.idea.utils.ExceptionTracker;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.util.Computable;
import com.intellij.openapi.vfs.VirtualFile;

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

    public void forget(Collection<SyncNote> notes) {
        SyncChangeData data = getSyncChangeData();

        for (SyncNote note : notes) {
            data.forget(note.getKey());
        }

        saveSyncChangeData();
    }

    public void forget(String key) {
        getSyncChangeData().forget(key);
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
        fileSystem.getSyncFile(new Callback<VirtualFile>() {
            @Override
            public void call(VirtualFile syncFile) {
                try {
                    syncFile.setBinaryContent(new Gson().toJson(syncChangeData).getBytes("UTF-8"));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private SyncChangeData getSyncChangeData() {
        if (syncChangeData == null) {
            syncChangeData = ApplicationManager.getApplication().runReadAction(new Computable<SyncChangeData>() {
                @Override
                public SyncChangeData compute() {
                    try {
                        SyncChangeData answer = null;
                        VirtualFile syncFile = fileSystem.getSyncFile();

                        if (syncFile != null) {
                            answer = new Gson().fromJson(new InputStreamReader(syncFile.getInputStream()), SyncChangeData.class);
                        }

                        if (answer != null) {
                            return answer;
                        } else {
                            return new SyncChangeData();
                        }
                    } catch (IOException e) {
                        ExceptionTracker.getInstance().trackAndRethrow(e);
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
