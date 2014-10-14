/*
 * AuthService
 * Copyright (c) 2012 Cybervision. All rights reserved.
 */
package com.hashmem.idea.remote;

import com.google.common.eventbus.Subscribe;
import com.hashmem.idea.NotAuthenticatedException;
import com.hashmem.idea.Router;
import com.hashmem.idea.SettingsService;
import com.hashmem.idea.event.SettingsChangeEvent;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.fluent.Request;

import java.io.IOException;

public class AuthService {

    private SettingsService settingsService;
    private HttpService httpService;
    private Router router;

    private volatile String token = null;

    public synchronized String getToken() throws NotAuthenticatedException {
        if (token == null) {
            try {
                HttpResponse r = Request.Post(router.getAuth()).execute().returnResponse();

                if (r.getStatusLine().getStatusCode() == 200) {
                    token = IOUtils.toString(r.getEntity().getContent(), "UTF-8");
                } else {
                    throw new NotAuthenticatedException();
                }
            } catch (IOException e) {
                throw new NotAuthenticatedException();
            }
        }

        return token;
    }

    public synchronized String getTokenOrEmpty() {
        try {
            return getToken();
        } catch (NotAuthenticatedException e) {
            return "";
        }
    }

    public synchronized String refreshToken() throws NotAuthenticatedException {
        token = null;
        return getToken();
    }

    @Subscribe
    public void onSettingsChange(final SettingsChangeEvent e) {
        token = null;
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
