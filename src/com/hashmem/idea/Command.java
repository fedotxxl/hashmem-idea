/*
 * HmCommand
 * Copyright (c) 2012 Cybervision. All rights reserved.
 */
package com.hashmem.idea;

import com.hashmem.idea.ui.Keyable;

public enum Command implements Keyable {

    FEEDBACK("feedback"), SETTINGS("settings"), SYNC("sync");

    private String key;

    Command(String key) {
        this.key = key;
    }

    public String getKey() {
        return key;
    }

    public static Command myValueOf(String key) {
        for (Command c : values()) {
            if (c.key.equals(key)) return c;
        }

        return null;
    }
}

