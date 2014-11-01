/*
 * SettingsService
 * Copyright (c) 2012 Cybervision. All rights reserved.
 */
package com.hashmem.idea.service;

import com.google.common.eventbus.Subscribe;
import com.hashmem.idea.HashMemSettings;
import com.hashmem.idea.event.SettingsChangeEvent;

public class SettingsService {

    public static final String APPLICATION_ID = "idea-1.0";

    private HashMemSettings.Model model;

    public SettingsService(HashMemSettings.Model model) {
        this.model = model;
    }

    private static String server = "http://localhost:8080/";

    public String getApplicationId() {
        return APPLICATION_ID;
    }

    public String getUsername() {
        return model.getUsername();
    }

    public String getPassword() {
        return model.getPassword();
    }

    public String getSyncServer() {
        return server;
    }

    public boolean isSyncEnabled() {
        return model.isSyncEnabled();
    }

    @Subscribe
    public void onSettingsChange(final SettingsChangeEvent e) {
        model = e.getModel();
    }

}
