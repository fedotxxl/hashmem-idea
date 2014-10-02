/*
 * Note
 * Copyright (c) 2012 Cybervision. All rights reserved.
 */
package com.hashmem.idea;

public class Note {

    private boolean deleted;
    private long lastUpdated;
    private String key;
    private String content;

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
