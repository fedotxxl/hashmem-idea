/*
 * NoteService
 * Copyright (c) 2012 Cybervision. All rights reserved.
 */
package com.hashmem.idea;

import com.google.common.collect.Lists;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.Function;
import org.apache.commons.lang.StringUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.regex.Pattern;

import static com.intellij.util.containers.ContainerUtil.map;

public class NotesService {

    private static final Pattern VALIDATE_KEY = Pattern.compile("^[0-9a-zA-Z\\.\\-_]*$");

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

    public Collection<Note> getNotesChangedSince(long since) {
        return mapFiles(fileSystem.getNotesChangesSince(since), false);
    }

    public Collection<Note> getNotesDeletedSince(long since) {
        return mapFiles(fileSystem.getNotesDeletedSince(since), true);
    }

    private Collection<Note> mapFiles(Collection<VirtualFile> files, boolean isDeleted) {
        ArrayList<Note> answer = Lists.newArrayList();

        for (VirtualFile file : files) {
            try {
                answer.add(new Note(file, isDeleted));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return answer;
    }

    public boolean isValidKey(String key) {
        return !StringUtils.isEmpty(key) && VALIDATE_KEY.matcher(key).matches();
    }

    //=========== SETTERS ============
    public void setFileSystem(FileSystem fileSystem) {
        this.fileSystem = fileSystem;
    }
}
