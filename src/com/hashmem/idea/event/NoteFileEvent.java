/*
 * NoteFileEvent
 * Copyright (c) 2012 Cybervision. All rights reserved.
 */
package com.hashmem.idea.event;

import com.intellij.openapi.vfs.VirtualFile;

public class NoteFileEvent {

    private String key;
    private VirtualFile file;

    public NoteFileEvent(String key, VirtualFile file) {
        this.key = key;
        this.file = file;
    }

    public String getKey() {
        return key;
    }

    public VirtualFile getFile() {
        return file;
    }
}
