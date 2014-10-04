/*
 * HmLog
 * Copyright (c) 2012 Cybervision. All rights reserved.
 */
package com.hashmem.idea.ui;

public class HmLog {

    public void noteNoteFound(String key) {
        warn("Note with key '" + key + "' is not found");
    }

    public void canNotCreateFile(String key) {
        warn("Can't create note file with key '" + key + "'");
    }

    private void warn(String message) {
        System.out.println(message);
    }

}
