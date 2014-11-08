/*
 * HashMemFileWritingAccessController
 * Copyright (c) 2012 Cybervision. All rights reserved.
 */
package com.hashmem.idea;

import com.hashmem.idea.service.FileSystem;
import com.intellij.openapi.fileEditor.impl.NonProjectFileWritingAccessExtension;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;

public class HashMemFileWritingAccessController implements NonProjectFileWritingAccessExtension {

    private FileSystem fileSystem = new FileSystem(); //bad idea but I don't know how to inject it =(

    @Override
    public boolean isWritable(@NotNull VirtualFile file) {
        if (fileSystem == null) {
            return false;
        } else {
            return fileSystem.isNote(file);
        }
    }

    public void setFileSystem(FileSystem fileSystem) {
        this.fileSystem = fileSystem;
    }
}
