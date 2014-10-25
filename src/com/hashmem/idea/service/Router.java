/*
 * Router
 * Copyright (c) 2012 Cybervision. All rights reserved.
 */
package com.hashmem.idea.service;

import com.hashmem.idea.remote.AuthService;
import org.apache.http.client.utils.URIBuilder;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;

public class Router {

    private AuthService authService;
    private SettingsService settingsService;

    public String getSync(final String token) {
        return getUrl("api/v1.1/sync", new UrlConstructor() {
            @Override
            public void construct(URIBuilder builder) {
                builder
                        .addParameter("applicationId", settingsService.getApplicationId())
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
            public void construct(URIBuilder builder) {
                builder
                        .addParameter("applicationId", settingsService.getApplicationId())
                        .addParameter("username", username)
                        .addParameter("password", password);
            }
        }).toString();
    }

    public URL getOpenNote(final String key) {
        return getUrl("auth/login_token", new UrlConstructor() {
            @Override
            public void construct(URIBuilder builder) {
                builder
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
        try {
            URIBuilder builder = new URIBuilder(settingsService.getSyncServer() + page);
            if (urlConstructor != null) urlConstructor.construct(builder);

            return builder.build().toURL();
        } catch (URISyntaxException e) {
            e.printStackTrace();
            return null;
        } catch (MalformedURLException e) {
            e.printStackTrace();
            return null;
        }
    }

    private static interface UrlConstructor {
        void construct(URIBuilder builder);
    }


    //=========== SETTERS ============
    public void setSettingsService(SettingsService settingsService) {
        this.settingsService = settingsService;
    }

    public void setAuthService(AuthService authService) {
        this.authService = authService;
    }
}
