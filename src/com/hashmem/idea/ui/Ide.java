/*
 * Idea
 * Copyright (c) 2012 Cybervision. All rights reserved.
 */
package com.hashmem.idea.ui;

import com.hashmem.idea.FileSystem;
import com.hashmem.idea.Note;
import com.intellij.openapi.fileEditor.OpenFileDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;

public class Ide {

    private FileSystem fileSystem;

    public void openNote(Note note, Project project) {
        VirtualFile file = fileSystem.virtualFileBy(note.getKey());
        if (file != null) {
            new OpenFileDescriptor(project, file).navigate(true);
        } else {

        }
    }

    //=========== SETTERS ============
    public void setFileSystem(FileSystem fileSystem) {
        this.fileSystem = fileSystem;
    }
}
