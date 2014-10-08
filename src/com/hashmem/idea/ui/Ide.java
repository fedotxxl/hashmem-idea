/*
 * Idea
 * Copyright (c) 2012 Cybervision. All rights reserved.
 */
package com.hashmem.idea.ui;

import com.hashmem.idea.FileSystem;
import com.intellij.ide.BrowserUtil;
import com.intellij.openapi.fileEditor.OpenFileDescriptor;
import com.intellij.openapi.fileTypes.PlainTextFileType;
import com.intellij.openapi.fileTypes.impl.FileTypeManagerImpl;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;

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
        OpenFileDescriptor descriptor = new OpenFileDescriptor(project, file);
        if (!descriptor.navigateInEditor(project, true)) {
            //open in text editor. Holy shit Jetbrains. Your ***** API is sucks
            FileTypeManagerImpl.cacheFileType(file, PlainTextFileType.INSTANCE);
            new OpenFileDescriptor(project, file).navigate(true);
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
