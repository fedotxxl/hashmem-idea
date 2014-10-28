/*
 * NoteService
 * Copyright (c) 2012 Cybervision. All rights reserved.
 */
package com.hashmem.idea.service;

import com.hashmem.idea.domain.Note;
import com.hashmem.idea.domain.SyncNote;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.Function;

import java.io.IOException;
import java.util.List;

import static com.intellij.util.containers.ContainerUtil.map;

public class NotesService {

    private FileSystem fileSystem;

    public void save(SyncNote note) {
        fileSystem.createNoteFile(note.getKey(), note.getContent());
    }

    public boolean has(String key) {
        return fileSystem.isNoteFileExists(key);
    }

    public boolean remove(String key) {
        return fileSystem.removeNote(key);
    }

    public List<Note> getNotes() {
        return map(fileSystem.listNotesKeys(), new Function<String, Note>() {
            @Override
            public Note fun(String it) {
                return new Note(it);
            }
        });
    }

    public Note getNote(String key) {
        VirtualFile file = fileSystem.virtualFileBy(key);

        if (file != null) {
            try {
                return new Note(file);
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        } else {
            return null;
        }
    }

    /**
     * Removes all notes and sync data
     */
    public void removeAllNotes() {
        fileSystem.removeAllNotes();
    }

    public String getKey(VirtualFile file) {
        return fileSystem.getNoteKey(file);
    }

    //=========== SETTERS ============
    public void setFileSystem(FileSystem fileSystem) {
        this.fileSystem = fileSystem;
    }
}
