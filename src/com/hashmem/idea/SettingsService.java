/*
 * SettingsService
 * Copyright (c) 2012 Cybervision. All rights reserved.
 */
package com.hashmem.idea;

public class SettingsService {

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
        return "https://hashMem.com/";
    }

    public boolean isSyncEnabled() {
        return true;
    }
}
