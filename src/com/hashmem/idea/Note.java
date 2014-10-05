/*
 * Note
 * Copyright (c) 2012 Cybervision. All rights reserved.
 */
package com.hashmem.idea;

import com.hashmem.idea.ui.Keyable;
import com.intellij.openapi.util.io.StreamUtil;
import com.intellij.openapi.vfs.VirtualFile;

import java.io.IOException;

public class Note implements Keyable {

    private boolean deleted;
    private long lastUpdated;
    private String key;
    private String content;

    public Note(VirtualFile file) throws IOException {
        this(file, false);
    }

    public Note(VirtualFile file, boolean deleted) throws IOException {
        key = file.getName();
        lastUpdated = FileSystem.getLastModified(file);
        content = StreamUtil.readText(file.getInputStream(), "UTF-8");
        this.deleted = deleted;
    }

    public Note(String key) {
        this.key = key;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public long getLastUpdated() {
        return lastUpdated;
    }

    public String getKey() {
        return key;
    }

    public String getContent() {
        return content;
    }

    public void setLastUpdated(long lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    @Override
    public String toString() {
        return "Note{" +
                "deleted=" + deleted +
                ", lastUpdated=" + lastUpdated +
                ", key='" + key + '\'' +
                ", content='" + content + '\'' +
                '}';
    }
}
