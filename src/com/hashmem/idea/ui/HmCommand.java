/*
 * HmCommand
 * Copyright (c) 2012 Cybervision. All rights reserved.
 */
package com.hashmem.idea.ui;

public enum HmCommand implements Keyable {

    FEEDBACK("feedback"), SETTINGS("settings"), SYNC("sync");

    private String key;

    HmCommand(String key) {
        this.key = key;
    }

    public String getKey() {
        return key;
    }
}

