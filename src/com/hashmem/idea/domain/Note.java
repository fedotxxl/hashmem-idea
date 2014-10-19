/*
 * Note
 * Copyright (c) 2012 Cybervision. All rights reserved.
 */
package com.hashmem.idea.domain;

import com.hashmem.idea.utils.Validator;
import com.intellij.openapi.util.io.StreamUtil;
import com.intellij.openapi.vfs.VirtualFile;

import java.io.IOException;

public class Note implements Keyable {

    private boolean deleted;
    private String key;
    private String content;
    private VirtualFile file;

    public Note(VirtualFile file) throws IOException {
        this(file, false);
    }

    public Note(VirtualFile file, boolean deleted) throws IOException {
        this.key = file.getName();
        this.content = StreamUtil.readText(file.getInputStream(), "UTF-8");
        this.deleted = deleted;
        this.file = file;
    }

    public Note(String key) {
        this.key = key;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public String getKey() {
        return key;
    }

    public String getContent() {
        return content;
    }

    public boolean isLinkContent() {
        return Validator.isLink(content);
    }

    public VirtualFile getFile() {
        return file;
    }

    @Override
    public String toString() {
        return "Note{" +
                "deleted=" + deleted +
                ", key='" + key + '\'' +
                ", content='" + content + '\'' +
                '}';
    }
}
