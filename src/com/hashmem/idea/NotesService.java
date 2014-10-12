/*
 * NoteService
 * Copyright (c) 2012 Cybervision. All rights reserved.
 */
package com.hashmem.idea;

import com.hashmem.NoteToSync;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.Function;
import org.apache.commons.lang.StringUtils;

import java.io.IOException;
import java.util.List;
import java.util.regex.Pattern;

import static com.intellij.util.containers.ContainerUtil.map;

public class NotesService {

    private static final Pattern VALIDATE_KEY = Pattern.compile("^[0-9a-zA-Z\\.\\-_]*$");

    private FileSystem fileSystem;

    public void save(NoteToSync note) {
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

    public String getKey(VirtualFile file) {
        return fileSystem.getNoteKey(file);
    }

    public boolean isValidKey(String key) {
        return !StringUtils.isEmpty(key) && VALIDATE_KEY.matcher(key).matches();
    }

    //=========== SETTERS ============
    public void setFileSystem(FileSystem fileSystem) {
        this.fileSystem = fileSystem;
    }
}
