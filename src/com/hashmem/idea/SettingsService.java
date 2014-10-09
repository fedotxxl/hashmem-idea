/*
 * SettingsService
 * Copyright (c) 2012 Cybervision. All rights reserved.
 */
package com.hashmem.idea;

import com.intellij.openapi.vfs.VirtualFile;
import org.apache.commons.lang.StringUtils;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class SettingsService {

    private String username;
    private String password;

    public static final String DEFAULT_CONTENT = "username=\npassword=";

    private static String server = "http://localhost:8080/";

    public String getApplicationId() {
        return "idea";
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getSyncServer() {
        return server;
    }

    public boolean isSyncEnabled() {
        return !StringUtils.isEmpty(username);
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
