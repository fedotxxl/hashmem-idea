/*
 * SettingsService
 * Copyright (c) 2012 Cybervision. All rights reserved.
 */
package com.hashmem.idea;

import com.intellij.openapi.vfs.VirtualFile;
import org.apache.commons.lang.StringUtils;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Properties;

public class SettingsService {

    private String username;
    private String password;
    private Integer syncPeriodInSeconds;

    public static final String DEFAULT_CONTENT = "username=\npassword=\nsyncEverySeconds=-1";

    private static String server = "https://hashMem.com/";

    public String getApplicationId() {
        return "idea";
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public Integer getSyncPeriodicityInSeconds() {
        return syncPeriodInSeconds;
    }

    public String getSyncServer() {
        return server;
    }

    public boolean isSyncEnabled() {
        return !StringUtils.isEmpty(username);
    }

    public URL getUrl(String page) throws MalformedURLException {
        return new URL(server + page);
    }

    public void refresh(VirtualFile file) {
        Properties prop = new Properties();
        InputStream input = null;

        try {

            input = file.getInputStream();

            // load a properties file
            prop.load(input);

            username = prop.getProperty("username");
            password = prop.getProperty("password");
            syncPeriodInSeconds = Integer.valueOf(prop.getProperty("syncEverySeconds", "-1"));

        } catch (IOException ex) {
            ex.printStackTrace();
        } finally {
            if (input != null) {
                try {
                    input.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
