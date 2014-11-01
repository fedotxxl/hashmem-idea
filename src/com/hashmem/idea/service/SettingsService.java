/*
 * SettingsService
 * Copyright (c) 2012 Cybervision. All rights reserved.
 */
package com.hashmem.idea.service;

import com.google.common.eventbus.Subscribe;
import com.hashmem.idea.HashMemBundle;
import com.hashmem.idea.HashMemSettings;
import com.hashmem.idea.event.SettingsChangeEvent;

public class SettingsService {

    public static final String APPLICATION_ID = HashMemBundle.message("application.id");
    private static final String SERVER = HashMemBundle.message("server.location");

    private HashMemSettings.Model model;

    public SettingsService(HashMemSettings.Model model) {
        this.model = model;
    }

    public String getUsername() {
        return model.getUsername();
    }

    public String getPassword() {
        return model.getPassword();
    }

    public String getSyncServer() {
        return SERVER;
    }

    public boolean isSyncEnabled() {
        return model.isSyncEnabled();
    }

    @Subscribe
    public void onSettingsChange(final SettingsChangeEvent e) {
        model = e.getModel();
    }

}
