/*
 * NoteService
 * Copyright (c) 2012 Cybervision. All rights reserved.
 */
package com.hashmem.idea;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NotesService {

    private Map<String, Note> notes = new HashMap<String, Note>();

    public void save(Note note) {
        notes.put(note.getKey(), note);
    }

    public List<Note> getNotes() {
        return new ArrayList<Note>(notes.values());
    }

}
