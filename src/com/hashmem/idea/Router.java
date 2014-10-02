/*
 * Router
 * Copyright (c) 2012 Cybervision. All rights reserved.
 */
package com.hashmem.idea;

import org.apache.http.client.utils.URIBuilder;

import java.net.URISyntaxException;

public class Router {

    private String sync;
    private SettingsService settingsService;

    public String getSync(String token) {
        return settingsService.getSyncServer() + "/api/v1/hm/sync?token=" + token;
    }

    public String getAuth() {
        try {
            return new URIBuilder(settingsService.getSyncServer() + "/api/v1/auth")
                    .addParameter("applicationId", settingsService.getApplicationId())
                    .addParameter("username", settingsService.getUsername())
                    .addParameter("password", settingsService.getPassword()).toString();
        } catch (URISyntaxException e) {
            return null;
        }
    }

    //=========== SETTERS ============
    public void setSettingsService(SettingsService settingsService) {
        this.settingsService = settingsService;
    }
}
