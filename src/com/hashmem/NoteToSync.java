/*
 * NoteToSync
 * Copyright (c) 2012 Cybervision. All rights reserved.
 */
package com.hashmem;

import com.hashmem.idea.Note;

public class NoteToSync {

    private boolean deleted;
    private long lastUpdated;
    private String key;
    private String content;

    public NoteToSync(Note note, long lastUpdated) {
        this.deleted = note.isDeleted();
        this.key = note.getKey();
        this.content = note.getContent();
        this.lastUpdated = lastUpdated;
    }

    private NoteToSync(String key, long lastUpdated, boolean deleted) {
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

    public static NoteToSync newDeletedNote(String key, long lastUpdated) {
        return new NoteToSync(key, lastUpdated, true);
    }
}
