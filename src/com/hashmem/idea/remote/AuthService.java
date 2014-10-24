/*
 * AuthService
 * Copyright (c) 2012 Cybervision. All rights reserved.
 */
package com.hashmem.idea.remote;

import com.google.common.eventbus.Subscribe;
import com.hashmem.idea.service.Router;
import com.hashmem.idea.service.SettingsService;
import com.hashmem.idea.event.SettingsChangeEvent;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.fluent.Request;

import java.io.IOException;

public class AuthService {

    private SettingsService settingsService;
    private HttpService httpService;
    private Router router;

    private volatile String token = null;

    public synchronized String getToken() throws NotAuthenticatedException, IOException {
        if (token == null) {
            token = getToken(settingsService.getUsername(), settingsService.getPassword());
        }

        return token;
    }

    public synchronized String getValidToken() throws NotAuthenticatedException, IOException {
        if (token == null || isTokenCorrect(token)) {
            return getToken();
        } else {
            return refreshToken();
        }
    }

    public Result tryAuthenticate(String username, String password) {
        try {
            String token = getToken(username, password);

            if (!StringUtils.isEmpty(token)) {
                return Result.SUCCESS;
            } else {
                return Result.UNKNOWN_EXCEPTION;
            }
        } catch (NotAuthenticatedException e) {
            return Result.NOT_AUTHENTICATED;
        } catch (IOException e) {
            return Result.IO_EXCEPTION;
        }
    }

    private String getToken(String username, String password) throws NotAuthenticatedException, IOException {
        HttpResponse r = Request.Post(router.getAuth(username, password)).execute().returnResponse();

        if (r.getStatusLine().getStatusCode() == 200) {
            return IOUtils.toString(r.getEntity().getContent(), "UTF-8");
        } else {
            throw new NotAuthenticatedException();
        }
    }

    private boolean isTokenCorrect(String token) {
        try {
            HttpResponse r = Request.Get(router.getPing(token)).execute().returnResponse();
            return r.getStatusLine().getStatusCode() == 200;
        } catch (IOException e) {
            return false;
        }
    }

    public synchronized String getValidTokenOrEmpty() {
        try {
            return getValidToken();
        } catch (NotAuthenticatedException e) {
            return "";
        } catch (IOException e) {
            return "";
        }
    }

    public synchronized String refreshToken() throws NotAuthenticatedException, IOException {
        token = null;
        return getToken();
    }

    @Subscribe
    public void onSettingsChange(final SettingsChangeEvent e) {
        token = null;
    }

    public static enum Result {
        SUCCESS, NOT_AUTHENTICATED, IO_EXCEPTION, UNKNOWN_EXCEPTION
    }

    //=========== SETTERS ============
    public void setSettingsService(SettingsService settingsService) {
        this.settingsService = settingsService;
    }

    public void setHttpService(HttpService httpService) {
        this.httpService = httpService;
    }

    public void setRouter(Router router) {
        this.router = router;
    }
}
