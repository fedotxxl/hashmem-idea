/*
 * Idea
 * Copyright (c) 2012 Cybervision. All rights reserved.
 */
package com.hashmem.idea.ui;

import com.hashmem.idea.service.FileSystem;
import com.hashmem.idea.utils.IOUtils;
import com.intellij.ide.BrowserUtil;
import com.intellij.openapi.fileEditor.OpenFileDescriptor;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.fileTypes.FileTypeManager;
import com.intellij.openapi.fileTypes.PlainTextFileType;
import com.intellij.openapi.fileTypes.UnknownFileType;
import com.intellij.openapi.fileTypes.impl.FileTypeManagerImpl;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;

import java.io.IOException;
import java.net.URL;

public class Ide {

    private FileSystem fileSystem;
    private HmLog log = new HmLog();

    public void openNote(String key, Project project) {
        VirtualFile file = fileSystem.virtualFileBy(key);
        if (file != null) {
            new OpenFileDescriptor(project, file).navigate(true);
        } else {
            log.noteNoteFound(key);
        }
    }

    public void open(VirtualFile file, Project project) {
        int fileLength = getFileLength(file);
        FileTypeManager fileTypeManager = FileTypeManager.getInstance();
        FileType fileType = fileTypeManager.getFileTypeByFile(file);

        if (fileType instanceof UnknownFileType) {
            FileTypeManagerImpl.cacheFileType(file, PlainTextFileType.INSTANCE);
        }

        new OpenFileDescriptor(project, file, (fileLength > 0) ? fileLength : 0).navigate(true);
    }

    private int getFileLength(VirtualFile file) {
        try {
            return IOUtils.toString(file.getInputStream()).length();
        } catch (IOException e) {
            return -1;
        }
    }

    public void openBrowser(String url) {
        BrowserUtil.browse(url);
    }

    public void openBrowser(URL url) {
        BrowserUtil.browse(url);
    }

    public HmLog getLog() {
        return log;
    }

    //=========== SETTERS ============
    public void setFileSystem(FileSystem fileSystem) {
        this.fileSystem = fileSystem;
    }
}
