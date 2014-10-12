/*
 * NoteFileChangedEvent
 * Copyright (c) 2012 Cybervision. All rights reserved.
 */
package com.hashmem.idea.event;

import com.intellij.openapi.vfs.VirtualFile;

public class NoteFileChangedEvent extends NoteFileEvent {

    public NoteFileChangedEvent(String key, VirtualFile file) {
        super(key, file);
    }

}
