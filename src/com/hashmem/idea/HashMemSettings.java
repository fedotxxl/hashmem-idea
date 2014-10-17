/*
 * HashMemSettings
 * Copyright (c) 2012 Cybervision. All rights reserved.
 */
package com.hashmem.idea;


import com.hashmem.idea.ui.HmSettingsDialog;
import com.intellij.ide.util.PropertiesComponent;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class HashMemSettings implements Configurable {

    private HmSettingsDialog form = null;
    private Model model;
    private SettingsService listener = null;

    public HashMemSettings() {
        model = Model.load();
    }

    @Nls
    @Override
    public String getDisplayName() {
        return "hashMem.com";
    }

    @Nullable
    @Override
    public String getHelpTopic() {
        return null;
    }

    @Nullable
    @Override
    public JComponent createComponent() {
        if (form == null) {
            form = new HmSettingsDialog();
        }

        return form.getRootComponent();
    }

    @Override
    public boolean isModified() {
        return false;
    }

    @Override
    public void apply() throws ConfigurationException {

    }

    @Override
    public void reset() {
    }

    @Override
    public void disposeUIResources() {
        form = null;
    }

    public Model getModel() {
        return model;
    }

    public void onModelChange(SettingsService listener) {
        this.listener = listener;
    }

    public static class Model {

        private String username;
        private String password;

        public Model(String username, String password) {
            this.username = username;
            this.password = password;
        }

        public String getUsername() {
            return username;
        }

        public String getPassword() {
            return password;
        }

        private void save() {
            set(Property.USERNAME, username);
            set(Property.USERNAME, password);
        }

        private static Model load() {
            String username = get(Property.USERNAME);
            String password = get(Property.PASSWORD);

            return new Model(username, password);
        }

        private static void set(Property property, String value) {
            PropertiesComponent.getInstance().setValue(property.getField(), value);
        }

        private static String get(Property property) {
            return PropertiesComponent.getInstance().getValue(property.getField());
        }
    }

    private static enum Property {

        USERNAME("username"), PASSWORD("password");

        private String postfix;

        Property(String postfix) {
            this.postfix = postfix;
        }

        public String getField() {
            return "hashMem." + postfix;
        }
    }
}
