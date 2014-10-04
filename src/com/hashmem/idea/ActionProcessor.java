/*
 * HmActionProcessor
 * Copyright (c) 2012 Cybervision. All rights reserved.
 */
package com.hashmem.idea;

import com.hashmem.idea.ui.Ide;
import com.hashmem.idea.ui.Query;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;

public class ActionProcessor {

    private Ide ide;
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
            return processCommand(key);
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
        return false;
    }

    private boolean processCommand(String command) {
        return false;
    }

    //=========== SETTERS ============
    public void setIde(Ide ide) {
        this.ide = ide;
    }

    public void setFileSystem(FileSystem fileSystem) {
        this.fileSystem = fileSystem;
    }
}
