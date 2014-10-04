package com.hashmem.idea;

import com.hashmem.idea.remote.AuthService;
import com.hashmem.idea.remote.HttpService;
import com.hashmem.idea.remote.SyncService;
import com.hashmem.idea.ui.Ide;
import com.hashmem.idea.ui.NotificationService;
import com.intellij.openapi.components.ApplicationComponent;
import org.jetbrains.annotations.NotNull;


public class HashMemApplicationComponent implements ApplicationComponent {

    private SyncService syncService;
    private NotesService notesService;
    private ActionProcessor actionProcessor;

    public HashMemApplicationComponent() {
    }

    public void initComponent() {
        syncService = new SyncService();
        notesService = new NotesService();
        actionProcessor = new ActionProcessor();

        Ide ide = new Ide();
        FileSystem fileSystem = new FileSystem();
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
        ide.setFileSystem(fileSystem);
        ide.setSettingsService(settingsService);

        notesService.setFileSystem(fileSystem);
        actionProcessor.setFileSystem(fileSystem);
        actionProcessor.setIde(ide);

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

    public ActionProcessor getActionProcessor() {
        return actionProcessor;
    }

    @NotNull
    public String getComponentName() {
        return "HashMemApplicationComponent";
    }
}
