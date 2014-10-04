/*
 * SettingsService
 * Copyright (c) 2012 Cybervision. All rights reserved.
 */
package com.hashmem.idea;

import java.net.MalformedURLException;
import java.net.URL;

public class SettingsService {

    private static String server = "https://hashMem.com/";

    public String getApplicationId() {
        return "idea";
    }

    public String getUsername() {
        return "fedotxxl2002@mail.ru";
    }

    public String getPassword() {
        return "belovf";
    }

    public Integer getSyncPeriodicityInSeconds() {
        return 120;
    }

    public String getSyncServer() {
        return server;
    }

    public boolean isSyncEnabled() {
        return true;
    }

    public URL getUrl(String page) throws MalformedURLException {
        return new URL(server + page);
    }
}
