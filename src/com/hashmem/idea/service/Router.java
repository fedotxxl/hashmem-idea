/*
 * Router
 * Copyright (c) 2012 Cybervision. All rights reserved.
 */
package com.hashmem.idea.service;

import com.hashmem.idea.remote.AuthService;
import gumi.builders.UrlBuilder;

import java.net.URL;

public class Router {

    private AuthService authService;
    private SettingsService settingsService;

    public String getSync(final String token) {
        return getUrl("api/v1.1/sync", new UrlConstructor() {
            @Override
            public UrlBuilder construct(UrlBuilder builder) {
                return builder
                        .addParameter("applicationId", SettingsService.APPLICATION_ID)
                        .addParameter("token", token);
            }
        }).toString();
    }

    public String getPing(String token) {
        return settingsService.getSyncServer() + "/api/v1/hm/ping?token=" + token;
    }

    public String getAuth() {
        return getAuth(settingsService.getUsername(), settingsService.getPassword());
    }

    public String getAuth(final String username, final String password) {
        return getUrl("api/v1/auth", new UrlConstructor() {
            @Override
            public UrlBuilder construct(UrlBuilder builder) {
                return builder
                        .addParameter("applicationId", SettingsService.APPLICATION_ID)
                        .addParameter("username", username)
                        .addParameter("password", password);
            }
        }).toString();
    }

    public URL getOpenNote(final String key) {
        return getUrl("auth/login_token", new UrlConstructor() {
            @Override
            public UrlBuilder construct(UrlBuilder builder) {
                return builder
                        .addParameter("mail", settingsService.getUsername())
                        .addParameter("token", authService.getValidTokenOrEmpty())
                        .addParameter("note", key);
            }
        });
    }

    public URL getHelp() {
        return getUrl("docs/plugin/jetbrains");
    }

    public URL getFeedback() {
        return getUrl("feedback");
    }

    private URL getUrl(String page) {
        return getUrl(page, null);
    }

    private URL getUrl(String page, UrlConstructor urlConstructor) {
            UrlBuilder builder = UrlBuilder.fromString(settingsService.getSyncServer() + page);
            if (urlConstructor != null) builder = urlConstructor.construct(builder);

            return builder.toUrl();
    }

    private static interface UrlConstructor {
        UrlBuilder construct(UrlBuilder builder);
    }

    //=========== SETTERS ============
    public void setSettingsService(SettingsService settingsService) {
        this.settingsService = settingsService;
    }

    public void setAuthService(AuthService authService) {
        this.authService = authService;
    }
}
