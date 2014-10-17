/*
 * SettingsService
 * Copyright (c) 2012 Cybervision. All rights reserved.
 */
package com.hashmem.idea;

import com.google.common.eventbus.EventBus;
import com.hashmem.idea.event.SettingsChangeEvent;
import com.intellij.openapi.application.ApplicationManager;
import org.apache.commons.lang.StringUtils;

public class SettingsService {

    private EventBus eventBus;
    private HashMemSettings.Model model;

    public SettingsService() {
        HashMemSettings settings = ApplicationManager.getApplication().getComponent(HashMemSettings.class);
        settings.onModelChange(this);
        model = settings.getModel();
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

    public void refresh(HashMemSettings.Model model) {
        this.model = model;

        eventBus.post(new SettingsChangeEvent());
    }

    //=========== SETTERS ============
    public void setEventBus(EventBus eventBus) {
        this.eventBus = eventBus;
    }
}
