/*
 * NoteService
 * Copyright (c) 2012 Cybervision. All rights reserved.
 */
package com.hashmem.idea;

import com.intellij.util.Function;

import java.util.List;

import static com.intellij.util.containers.ContainerUtil.map;

public class NotesService {

    private FileSystem fileSystem;

    public void save(Note note) {
        fileSystem.createFile(note.getKey(), note.getContent());
    }

    public List<Note> getNotes() {
        return map(fileSystem.listFiles(), new Function<String, Note>() {
            @Override
            public Note fun(String it) {
                return new Note(it);
            }
        });
    }

    //=========== SETTERS ============
    public void setFileSystem(FileSystem fileSystem) {
        this.fileSystem = fileSystem;
    }
}
