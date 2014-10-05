/*
 * Idea
 * Copyright (c) 2012 Cybervision. All rights reserved.
 */
package com.hashmem.idea.ui;

import com.hashmem.idea.FileSystem;
import com.hashmem.idea.SettingsService;
import com.intellij.ide.BrowserUtil;
import com.intellij.openapi.fileEditor.OpenFileDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;

import java.net.MalformedURLException;
import java.net.URL;

public class Ide {

    private FileSystem fileSystem;
    private SettingsService settingsService;
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
        new OpenFileDescriptor(project, file).navigate(true);
    }

    public void openBrowser(String page) {
        try {
            openBrowser(settingsService.getUrl(page));
        } catch (MalformedURLException e) {
            log.incorrectPageUrl(e.getMessage());
        }
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

    public void setSettingsService(SettingsService settingsService) {
        this.settingsService = settingsService;
    }
}
