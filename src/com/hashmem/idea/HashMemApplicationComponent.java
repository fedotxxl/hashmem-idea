package com.hashmem.idea;

import com.google.common.eventbus.EventBus;
import com.hashmem.idea.remote.AuthService;
import com.hashmem.idea.remote.HttpService;
import com.hashmem.idea.remote.SyncChangeService;
import com.hashmem.idea.remote.SyncService;
import com.hashmem.idea.service.*;
import com.hashmem.idea.ui.HmLog;
import com.hashmem.idea.ui.Ide;
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

    @Override
    public void initComponent() {
        //start plugin
        startApplicationContext();

        //sync on start
        syncService.syncOnChange();
    }

    @Override
    public void disposeComponent() {
        connection.disconnect();
    }

    private void startApplicationContext() {
        HashMemSettings settings = ApplicationManager.getApplication().getComponent(HashMemSettings.class);

        syncService = new SyncService();
        notesService = new NotesService();
        actionProcessor = new ActionProcessor();

        HmLog log = new HmLog();
        EventBus eventBus = new EventBus();
        Ide ide = new Ide();
        FileSystem fileSystem = new FileSystem();
        AuthService authService = new AuthService();
        HttpService httpService = HttpService.getInstance();
        Router router = new Router();
        SyncChangeService syncChangeService = new SyncChangeService();
        SettingsService settingsService = new SettingsService(settings.getModel());
        AccountService accountService = new AccountService();

        accountService.setNotesService(notesService);
        accountService.setSyncService(syncService);

        authService.setHttpService(httpService);
        authService.setRouter(router);
        authService.setSettingsService(settingsService);

        syncService.setSettingsService(settingsService);
        syncService.setRouter(router);
        syncService.setHttpService(httpService);
        syncService.setAuthService(authService);
        syncService.setNotesService(notesService);
        syncService.setLog(log);
        syncService.setSyncChangeService(syncChangeService);

        router.setSettingsService(settingsService);
        router.setAuthService(authService);
        fileSystem.setEventBus(eventBus);
        ide.setFileSystem(fileSystem);
        syncChangeService.setFileSystem(fileSystem);

        settings.setEventBus(eventBus);
        settings.setAccountService(accountService);
        settings.setAuthService(authService);

        notesService.setFileSystem(fileSystem);
        actionProcessor.setFileSystem(fileSystem);
        actionProcessor.setIde(ide);
        actionProcessor.setSyncService(syncService);
        actionProcessor.setRouter(router);
        actionProcessor.setNotesService(notesService);

        connection.subscribe(VirtualFileManager.VFS_CHANGES, fileSystem);
        eventBus.register(syncService);
        eventBus.register(authService);
        eventBus.register(settingsService);
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
