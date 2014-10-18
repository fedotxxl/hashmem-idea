/*
 * SettingsChangeEvent
 * Copyright (c) 2012 Cybervision. All rights reserved.
 */
package com.hashmem.idea.event;

import com.hashmem.idea.HashMemSettings;

public class SettingsChangeEvent {

    private HashMemSettings.Model model;

    public SettingsChangeEvent(HashMemSettings.Model model) {
        this.model = model;
    }

    public HashMemSettings.Model getModel() {
        return model;
    }
}
