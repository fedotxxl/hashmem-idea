/*
 * HmLog
 * Copyright (c) 2012 Cybervision. All rights reserved.
 */
package com.hashmem.idea.ui;

public class HmLog {

    public void noteNoteFound(String key) {
        noteNoteFound(key, true);
    }

    public void noteNoteFound(String key, boolean isInfo) {
        String message = "Note with key '" + key + "' is not found";

        if (isInfo) {
            info(message);
        } else {
            warn(message);
        }
    }

    public void canNotCreateFile(String key) {
        warn("Can't create note file with key '" + key + "'");
    }

    public void fileDeleted(String key) {
        info("Note '" + key + "' was successfully deleted");
    }
    public void canNotDeletedFile(String key) {
        info("Can't delete note file with '" + key + "'");
    }

    public void incorrectPageUrl(String url) {
        warn("Incorrect page url: '" + url + "'");
    }

    public void unknownCommand(String key) {
        warn("Unknown command: '" + key + "'");
    }


    private void warn(String message) {
        System.out.println(message);
    }
    private void info(String message) {
        System.out.println(message);
    }

}
