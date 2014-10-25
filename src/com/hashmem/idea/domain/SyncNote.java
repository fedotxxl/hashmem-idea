/*
 * NoteToSync
 * Copyright (c) 2012 Cybervision. All rights reserved.
 */
package com.hashmem.idea.domain;

public class SyncNote {

    private boolean deleted;
    private long lastUpdated;
    private String key;
    private String content;

    public SyncNote() {
    }

    public SyncNote(Note note, long lastUpdated) {
        this.deleted = note.isDeleted();
        this.key = note.getKey();
        this.content = note.getContent();
        this.lastUpdated = lastUpdated;
    }

    private SyncNote(String key, long lastUpdated, boolean deleted) {
        this.key = key;
        this.lastUpdated = lastUpdated;
        this.deleted = deleted;
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

    public static SyncNote newDeletedNote(String key, long lastUpdated) {
        return new SyncNote(key, lastUpdated, true);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SyncNote)) return false;

        SyncNote syncNote = (SyncNote) o;

        if (deleted != syncNote.deleted) return false;
        if (lastUpdated != syncNote.lastUpdated) return false;
        if (content != null ? !content.equals(syncNote.content) : syncNote.content != null) return false;
        if (!key.equals(syncNote.key)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = (deleted ? 1 : 0);
        result = 31 * result + (int) (lastUpdated ^ (lastUpdated >>> 32));
        result = 31 * result + key.hashCode();
        result = 31 * result + (content != null ? content.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "SyncNote{" +
                "deleted=" + deleted +
                ", lastUpdated=" + lastUpdated +
                ", key='" + key + '\'' +
                ", content='" + content + '\'' +
                '}';
    }
}
