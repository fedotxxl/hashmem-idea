/*
 * HmActionProcessor
 * Copyright (c) 2012 Cybervision. All rights reserved.
 */
package com.hashmem.idea.service;

import com.hashmem.idea.HashMemSettings;
import com.hashmem.idea.domain.Note;
import com.hashmem.idea.domain.Command;
import com.hashmem.idea.remote.SyncService;
import com.hashmem.idea.ui.HmLog;
import com.hashmem.idea.ui.Ide;
import com.hashmem.idea.domain.Query;
import com.intellij.openapi.options.ShowSettingsUtil;
import com.intellij.openapi.project.Project;

public class ActionProcessor {

    private Ide ide;
    private HmLog log;
    private NotesService notesService;
    private FileSystem fileSystem;
    private SyncService syncService;
    private Router router;

    public boolean processAction(Query query, Project project) {
        Query.Type type = query.getType();
        String key = query.getKey();

        if (!notesService.isValidKey(key)) {
            return false;
        }

        if (type == Query.Type.OPEN) {
            return open(key, project);
        } else if (type == Query.Type.CREATE) {
            return create(key, project);
        } else if (type == Query.Type.DELETE) {
            return delete(key);
        } else if (type == Query.Type.OPEN_SITE) {
            return openSite(key);
        } else if (type == Query.Type.COMMAND) {
            return processCommand(key, project);
        }

        return false;
    }

    private boolean open(String key, Project project) {
        return open(key, project, true);
    }

    private boolean open(String key, Project project, boolean checkIsLink) {
        Note note = notesService.getNote(key);
        if (note != null) {
            if (checkIsLink && note.isLinkContent()) {
                ide.openBrowser(note.getContent());
            } else {
                ide.open(note.getFile(), project);
            }

            return true;
        } else {
            ide.getLog().noteNoteFound(key);
            return false;
        }
    }

    private boolean openSite(String key) {
        ide.openBrowser(router.getOpenNote(key));
        return true;
    }

    private boolean create(String key, Project project) {
        if (!notesService.has(key)) {
            if (!fileSystem.createNoteFile(key, "")) {
                ide.getLog().canNotCreateFile(key);
                return false;
            }
        }

        return open(key, project, false);
    }

    private boolean delete(String key) {
        if (notesService.has(key)) {
            if (fileSystem.removeNote(key)) {
                log.fileDeleted(key);
                return true;
            } else {
                log.canNotDeletedFile(key);
                return false;
            }
        } else {
            log.noteNoteFound(key, true);
            return false;
        }
    }

    private boolean processCommand(String commandKey, Project project) {
        Command command = Command.myValueOf(commandKey);

        if (command == null) {
            log.unknownCommand(commandKey);
        } else if (command == Command.FEEDBACK) {
            ide.openBrowser(router.getFeedback());
            return true;
        } else if (command == Command.SETTINGS) {
            ShowSettingsUtil.getInstance().showSettingsDialog(project, HashMemSettings.class);
            return true;
        } else if (command == Command.SYNC) {
            syncService.syncAllNow();
            return true;
        } else if (command == Command.HELP) {
            ide.openBrowser(router.getHelp());
            return true;
        }

        return false;
    }

    //=========== SETTERS ============
    public void setIde(Ide ide) {
        this.ide = ide;
        this.log = ide.getLog();
    }

    public void setFileSystem(FileSystem fileSystem) {
        this.fileSystem = fileSystem;
    }

    public void setSyncService(SyncService syncService) {
        this.syncService = syncService;
    }

    public void setRouter(Router router) {
        this.router = router;
    }

    public void setNotesService(NotesService notesService) {
        this.notesService = notesService;
    }
}
