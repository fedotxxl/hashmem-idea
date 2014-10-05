/*
 * HmActionProcessor
 * Copyright (c) 2012 Cybervision. All rights reserved.
 */
package com.hashmem.idea;

import com.hashmem.idea.ui.HmLog;
import com.hashmem.idea.ui.Ide;
import com.hashmem.idea.ui.Query;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;

public class ActionProcessor {

    private Ide ide;
    private HmLog log;
    private FileSystem fileSystem;

    public boolean processAction(Query query, Project project) {
        Query.Type type = query.getType();
        String key = query.getKey();

        if (type == Query.Type.OPEN) {
            return open(key, project);
        } else if (type == Query.Type.CREATE) {
            return create(key, project);
        } else if (type == Query.Type.DELETE) {
            return delete(key);
        } else if (type == Query.Type.COMMAND) {
            return processCommand(key, project);
        }

        return false;
    }

    private boolean open(String key, Project project) {
        VirtualFile file = fileSystem.virtualFileBy(key);
        if (file != null) {
            ide.open(file, project);
            return true;
        } else {
            ide.getLog().noteNoteFound(key);
            return false;
        }
    }

    private boolean create(String key, Project project) {
        if (!fileSystem.scratchFileExists(key)) {
            if (!fileSystem.createFile(key, "")) {
                ide.getLog().canNotCreateFile(key);
                return false;
            }
        }

        return open(key, project);
    }

    private boolean delete(String key) {
        if (fileSystem.scratchFileExists(key)) {
            if (fileSystem.removeFile(key)) {
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
            ide.openBrowser("feedback");
            return true;
        } else if (command == Command.SETTINGS) {
            ide.open(fileSystem.getSettingsFile(), project);
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
}
