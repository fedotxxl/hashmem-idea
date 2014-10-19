/*
 * SettingsService
 * Copyright (c) 2012 Cybervision. All rights reserved.
 */
package com.hashmem.idea.service;

import com.google.common.eventbus.Subscribe;
import com.hashmem.idea.HashMemSettings;
import com.hashmem.idea.event.SettingsChangeEvent;
import org.apache.commons.lang.StringUtils;

public class SettingsService {

    private HashMemSettings.Model model;

    public SettingsService(HashMemSettings.Model model) {
        this.model = model;
    }

    private static String server = "http://localhost:8080/";

    public String getApplicationId() {
        return "idea";
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
        return !StringUtils.isEmpty(getUsername());
    }

    @Subscribe
    public void onSettingsChange(final SettingsChangeEvent e) {
        model = e.getModel();
    }

}
