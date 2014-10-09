/*
 * SyncChangeService
 * Copyright (c) 2012 Cybervision. All rights reserved.
 */
package com.hashmem.idea.remote;

import com.google.gson.Gson;
import com.hashmem.idea.FileSystem;
import com.hashmem.idea.Startable;
import com.intellij.openapi.application.ApplicationManager;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

public class SyncChangeService implements Startable {

    private FileSystem fileSystem;
    private SyncChangeData syncChangeData;

    void markAsDeleted(String key) {
        markAsDeleted(key, System.currentTimeMillis());
    }

    void markAsUpdated(String key) {
        markAsUpdated(key, System.currentTimeMillis());
    }

    void markAsDeleted(String key, long date) {
        syncChangeData.markAsDeleted(key, date);
        saveSyncChangeData();
    }

    void markAsUpdated(String key, long date) {
        syncChangeData.markAsUpdated(key, date);
        saveSyncChangeData();
    }

    List<String> getUpdatedSince(long since) {
        return syncChangeData.getUpdatedSince(since);
    }

    List<String> getDeletedSince(long since) {
       return syncChangeData.getDeletedSince(since);
    }

    Long getLastUpdated(String key) {
        return syncChangeData.getLastUpdated(key);
    }

    @Override
    public void postConstruct() {
        readSyncChangeData();
    }

    private void readSyncChangeData() {
        ApplicationManager.getApplication().executeOnPooledThread(new Runnable() {
            public void run() {
                try {
                    syncChangeData = new Gson().fromJson(new InputStreamReader(fileSystem.getSyncFile().getInputStream()), SyncChangeData.class);
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    if (syncChangeData == null) syncChangeData = new SyncChangeData();
                }
            }
        });
    }

    private void saveSyncChangeData() {
        try {
            fileSystem.getSyncFile().setBinaryContent(new Gson().toJson(syncChangeData).getBytes("UTF-8"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //=========== SETTERS ============
    public void setFileSystem(FileSystem fileSystem) {
        this.fileSystem = fileSystem;
    }
}
