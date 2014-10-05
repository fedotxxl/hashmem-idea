/*
 * Router
 * Copyright (c) 2012 Cybervision. All rights reserved.
 */
package com.hashmem.idea;

import com.hashmem.idea.remote.AuthService;
import org.apache.http.client.utils.URIBuilder;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;

public class Router {

    private AuthService authService;
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
            e.printStackTrace();
            return null;
        }
    }

    public URL getOpenNote(String key) {
        try {
            return new URIBuilder(settingsService.getSyncServer() + "/auth/login_token")
                    .addParameter("mail", settingsService.getUsername())
                    .addParameter("token", authService.getTokenOrEmpty())
                    .addParameter("note", key).build().toURL();
        } catch (URISyntaxException e) {
            e.printStackTrace();
            return null;
        } catch (MalformedURLException e) {
            e.printStackTrace();
            return null;
        }
    }

    //=========== SETTERS ============
    public void setSettingsService(SettingsService settingsService) {
        this.settingsService = settingsService;
    }

    public void setAuthService(AuthService authService) {
        this.authService = authService;
    }
}
