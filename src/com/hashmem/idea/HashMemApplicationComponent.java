package com.hashmem.idea;

import com.hashmem.idea.remote.AuthService;
import com.hashmem.idea.remote.HttpService;
import com.hashmem.idea.remote.SyncService;
import com.hashmem.idea.ui.NotificationService;
import com.intellij.openapi.components.ApplicationComponent;
import org.jetbrains.annotations.NotNull;


public class HashMemApplicationComponent implements ApplicationComponent {

    private SyncService syncService;

    public HashMemApplicationComponent() {
    }

    public void initComponent() {
        AuthService authService = new AuthService();
        HttpService httpService = new HttpService();
        syncService = new SyncService();
        NotificationService notificationService = new NotificationService();
        NotesService notesService = new NotesService();
        Router router = new Router();
        SettingsService settingsService = new SettingsService();

        authService.setHttpService(httpService);
        authService.setRouter(router);
        authService.setSettingsService(settingsService);

        syncService.setSettingsService(settingsService);
        syncService.setRouter(router);
        syncService.setHttpService(httpService);
        syncService.setAuthService(authService);
        syncService.setNotesService(notesService);
        syncService.setNotificationService(notificationService);

        router.setSettingsService(settingsService);

        syncService.postConstruct();
    }

    public void disposeComponent() {
        // TODO: insert component disposal logic here
    }

    public SyncService getSyncService() {
        return syncService;
    }

    @NotNull
    public String getComponentName() {
        return "HashMemApplicationComponent";
    }
}
