/*
 * NoteFileDeletedEvent
 * Copyright (c) 2012 Cybervision. All rights reserved.
 */
package com.hashmem.idea.event;

import com.intellij.openapi.vfs.VirtualFile;

public class NoteFileDeletedEvent extends NoteFileEvent {

    public NoteFileDeletedEvent(String key, VirtualFile file) {
        super(key, file);
    }

}
