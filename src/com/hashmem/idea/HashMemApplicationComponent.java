package com.hashmem.idea;

import com.hashmem.idea.remote.AuthService;
import com.hashmem.idea.remote.HttpService;
import com.hashmem.idea.remote.SyncService;
import com.hashmem.idea.ui.Ide;
import com.hashmem.idea.ui.NotificationService;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.ApplicationComponent;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.util.messages.MessageBusConnection;
import org.jetbrains.annotations.NotNull;


public class HashMemApplicationComponent implements ApplicationComponent {

    private SyncService syncService;
    private NotesService notesService;
    private ActionProcessor actionProcessor;
    private MessageBusConnection connection;

    public HashMemApplicationComponent() {
        connection = ApplicationManager.getApplication().getMessageBus().connect();
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
        router.setAuthService(authService);
        fileSystem.setSettingsService(settingsService);
        fileSystem.setSyncService(syncService);
        ide.setFileSystem(fileSystem);

        notesService.setFileSystem(fileSystem);
        actionProcessor.setFileSystem(fileSystem);
        actionProcessor.setIde(ide);
        actionProcessor.setSyncService(syncService);
        actionProcessor.setRouter(router);

        connection.subscribe(VirtualFileManager.VFS_CHANGES, fileSystem);
        fileSystem.postConstruct();
    }

    public void disposeComponent() {
        connection.disconnect();
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
