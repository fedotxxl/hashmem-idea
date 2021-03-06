/*
 * HashMemSettings
 * Copyright (c) 2012 Cybervision. All rights reserved.
 */
package com.hashmem.idea;


import com.google.common.eventbus.EventBus;
import com.hashmem.idea.event.SettingsChangeEvent;
import com.hashmem.idea.event.SettingsChangedEvent;
import com.hashmem.idea.i18n.MessageBundle;
import com.hashmem.idea.remote.AuthService;
import com.hashmem.idea.service.AccountService;
import com.hashmem.idea.ui.HmSettingsDialog;
import com.intellij.ide.util.PropertiesComponent;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class HashMemSettings implements Configurable {

    private AccountService accountService;
    private AuthService authService;
    private EventBus eventBus;

    private HmSettingsDialog form = null;
    private Model model;

    public HashMemSettings() {
        model = loadModel();
    }

    @Nls
    @Override
    public String getDisplayName() {
        return MessageBundle.message("settings.title");
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
            form = new HmSettingsDialog(model, authService);
        }

        return form.getRootComponent();
    }

    @Override
    public boolean isModified() {
        return form != null && (form.getAppliedAction() != null || isModelChanged());
    }

    private boolean isModelChanged() {
        return !model.equals(form.getModel());
    }

    @Override
    public void apply() throws ConfigurationException {
        if (form == null) return;

        Action formAction = form.getAppliedAction();
        Model formModel = form.getModel();

        if (formAction == Action.UNLINK_AND_RESET) {
            accountService.reset();
        }

        if (!model.equals(formModel)) {
            model = formModel;
            persistModel();
            eventBus.post(new SettingsChangeEvent(model));
            eventBus.post(new SettingsChangedEvent());
        }
    }

    @Override
    public void reset() {
        form.setModelAndResetAppliedAction(model);
    }

    @Override
    public void disposeUIResources() {
        form = null;
    }

    public Model getModel() {
        return model;
    }

    private Model loadModel() {
        String isSyncEnabled = get(Property.SYNC, "0");
        String username = get(Property.USERNAME, "");
        String password = get(Property.PASSWORD, "");

        return new Model("1".equals(isSyncEnabled), username, password);
    }

    private void persistModel() {
        set(Property.SYNC, (model.isSyncEnabled()) ? "1" : "0");
        set(Property.USERNAME, model.getUsername());
        set(Property.PASSWORD, model.getPassword());
    }

    private static void set(Property property, String value) {
        PropertiesComponent.getInstance().setValue(property.getField(), value);
    }

    private static String get(Property property, String defaultValue) {
        String answer =  PropertiesComponent.getInstance().getValue(property.getField());

        if (answer == null) {
            return defaultValue;
        } else {
            return answer;
        }
    }

    public static class Model {

        private boolean isSyncEnabled;
        private String username;
        private String password;

        public Model(boolean isSyncEnabled, String username, String password) {
            this.isSyncEnabled = isSyncEnabled;
            this.username = username;
            this.password = password;
        }

        public String getUsername() {
            return username;
        }

        public String getPassword() {
            return password;
        }

        public boolean isSyncEnabled() {
            return isSyncEnabled;
        }


        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Model)) return false;

            Model model = (Model) o;

            if (isSyncEnabled != model.isSyncEnabled) return false;
            if (password != null ? !password.equals(model.password) : model.password != null) return false;
            if (username != null ? !username.equals(model.username) : model.username != null) return false;

            return true;
        }

        @Override
        public int hashCode() {
            int result = (isSyncEnabled ? 1 : 0);
            result = 31 * result + (username != null ? username.hashCode() : 0);
            result = 31 * result + (password != null ? password.hashCode() : 0);
            return result;
        }
    }

    public static enum Action {
        UNLINK, UNLINK_AND_RESET
    }

    private static enum Property {

        SYNC("sync"), USERNAME("username"), PASSWORD("password");

        private String postfix;

        Property(String postfix) {
            this.postfix = postfix;
        }

        public String getField() {
            return "com.hashMem." + postfix;
        }
    }

    //=========== SETTERS ============
    public void setAccountService(AccountService accountService) {
        this.accountService = accountService;
    }

    public void setEventBus(EventBus eventBus) {
        this.eventBus = eventBus;
    }

    public void setAuthService(AuthService authService) {
        this.authService = authService;
    }
}
