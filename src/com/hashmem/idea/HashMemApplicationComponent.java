package com.hashmem.idea;

import com.hashmem.idea.remote.AuthService;
import com.hashmem.idea.remote.HttpService;
import com.hashmem.idea.remote.SyncService;
import com.hashmem.idea.ui.NotificationService;
import com.intellij.openapi.components.ApplicationComponent;
import org.jetbrains.annotations.NotNull;


public class HashMemApplicationComponent implements ApplicationComponent {

    private SyncService syncService;
    private NotesService notesService;

    public HashMemApplicationComponent() {
    }

    public void initComponent() {
        syncService = new SyncService();
        notesService = new NotesService();

        AuthService authService = new AuthService();
        HttpService httpService = new HttpService();
        NotificationService notificationService = new NotificationService();
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

    public NotesService getNotesService() {
        return notesService;
    }

    @NotNull
    public String getComponentName() {
        return "HashMemApplicationComponent";
    }
}
