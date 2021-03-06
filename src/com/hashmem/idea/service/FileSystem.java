package com.hashmem.idea.service;

import com.google.common.eventbus.EventBus;
import com.hashmem.idea.event.NoteFileChangedEvent;
import com.hashmem.idea.event.NoteFileDeletedEvent;
import com.hashmem.idea.i18n.MessageBundle;
import com.hashmem.idea.tracked.TrackedRunnable;
import com.hashmem.idea.utils.Callback;
import com.hashmem.idea.utils.ExceptionTracker;
import com.hashmem.idea.utils.OneTimeContainer;
import com.intellij.CommonBundle;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.PathManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.util.Computable;
import com.intellij.openapi.util.Condition;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.openapi.vfs.newvfs.BulkFileListener;
import com.intellij.openapi.vfs.newvfs.events.*;
import com.intellij.util.Function;
import com.intellij.util.containers.ContainerUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.List;

import static com.intellij.util.containers.ContainerUtil.findAll;
import static com.intellij.util.containers.ContainerUtil.map;


public class FileSystem implements BulkFileListener {
	private static final Logger LOG = Logger.getInstance(FileSystem.class);

    private EventBus eventBus;

	/**
	 * Use UTF-8 to be compatible with old version of plugin.
	 */
	private static final Charset CHARSET = Charset.forName("UTF8");
	private static final String HASHMEM_FOLDER = "";
	private static final String SYNC_FILE = ".sync";
    private static final String NOTES_FOLDER_PATH = PathManager.getPluginsPath() + "/hm/";

    private final VirtualFileManager fileManager = VirtualFileManager.getInstance();
	private final Condition<VirtualFile> canBeNote = new Condition<VirtualFile>() {
		@Override public boolean value(VirtualFile it) {
			return it != null && it.exists() && !it.isDirectory() && !isHidden(it.getName());
		}
	};
    private OneTimeContainer<String> notesToSkipChangeOrDeletedEvents = new OneTimeContainer<String>();

    private VirtualFile getRootFolder() {
        return virtualFileBy(HASHMEM_FOLDER);
    }

	public List<String> listNotesKeys() {
		VirtualFile virtualFile = getRootFolder();
		if (virtualFile == null || !virtualFile.exists()) {
			return Collections.emptyList();
		}
		return map(findAll(virtualFile.getChildren(), canBeNote), new Function<VirtualFile, String>() {
            @Override
            public String fun(VirtualFile it) {
                return it.getName();
            }
        });
	}

	public boolean isNoteFileExists(String key) {
		return canBeNote.value(virtualFileBy(key));
	}

    public boolean createNoteFile(String key, final String text) {
        return createFile(key, text, NOTES_FOLDER_PATH);
    }

    public boolean setNoteContent(String key, final String text) {
        return setContent(key, text, NOTES_FOLDER_PATH);
    }

    private boolean createFile(final String fileName, final String text, final String path) {
        return ApplicationManager.getApplication().runReadAction(new Computable<Boolean>() {

            @Override
            public Boolean compute() {
                return ApplicationManager.getApplication().runWriteAction(new Computable<Boolean>() {
                    @Override
                    public Boolean compute() {
                        try {
                            ensureExists(new File(path));

                            VirtualFile notesFolder = virtualFileBy(path, true);
                            if (notesFolder == null) return false;

                            VirtualFile noteFile = notesFolder.createChildData(FileSystem.this, fileName);
                            noteFile.setBinaryContent(text.getBytes(CHARSET));

                            return true;
                        } catch (IOException e) {
                            LOG.warn(e);
                            ExceptionTracker.getInstance().trackAndRethrow(e);
                            return false;
                        }
                    }
                });
            }
        });
	}

    private boolean setContent(final String fileName, final String text, final String path) {
        return ApplicationManager.getApplication().runReadAction(new Computable<Boolean>() {

            @Override
            public Boolean compute() {
                return ApplicationManager.getApplication().runWriteAction(new Computable<Boolean>() {
                    @Override
                    public Boolean compute() {
                        try {
                            ensureExists(new File(path));

                            VirtualFile noteFile = virtualFileBy(path + "/" + fileName, true);
                            if (noteFile == null) return false;

                            noteFile.setBinaryContent(text.getBytes(CHARSET));

                            return true;
                        } catch (IOException e) {
                            LOG.warn(e);
                            ExceptionTracker.getInstance().trackAndRethrow(e);
                            return false;
                        }
                    }
                });
            }
        });
    }

	public boolean removeNote(String key) {
	    return removeFile(getNoteFilePath(key));
	}

    public boolean removeFile(final String filePath) {
        return ApplicationManager.getApplication().runWriteAction(new Computable<Boolean>() {
            @Override
            public Boolean compute() {
                VirtualFile virtualFile = virtualFileBy(filePath, true);
                if (virtualFile == null) return false;

                try {
                    virtualFile.delete(FileSystem.this);
                    return true;
                } catch (IOException e) {
                    LOG.warn(e);
                    ExceptionTracker.getInstance().trackAndRethrow(e);
                    return false;
                }
            }
        });
    }

    public void removeAllNotes() {
        for (String key : listNotesKeys()) {
            notesToSkipChangeOrDeletedEvents.put(key);
            removeNote(key);
        }
    }

    @Nullable public VirtualFile virtualFileBy(String fileName) {
        return virtualFileBy(fileName, false);
    }

	@Nullable private VirtualFile virtualFileBy(String fileName, boolean isAbsolute) {
		return fileManager.refreshAndFindFileByUrl("file://" + ((isAbsolute) ? fileName : getFilePath(fileName)));
	}

    private String getNoteFilePath(String key) {
        return getFilePath(key);
    }

    private String getFilePath(String fileName) {
        return NOTES_FOLDER_PATH + fileName;
    }

	public boolean isNote(final VirtualFile virtualFile) {
		VirtualFile notesFolder = getRootFolder();
		return notesFolder != null && !isHidden(virtualFile.getName()) && ContainerUtil.exists(notesFolder.getChildren(), new Condition<VirtualFile>() {
            @Override
            public boolean value(VirtualFile it) {
                return it.equals(virtualFile);
            }
        });
	}

	private static boolean isHidden(String fileName) {
		return fileName.startsWith(".");
	}

	private static void ensureExists(File dir) throws IOException {
		if (!dir.exists() && !dir.mkdirs()) {
			throw new IOException(CommonBundle.message("exception.directory.can.not.create", dir.getPath()));
		}
	}

    @Override
    public void before(@NotNull List<? extends VFileEvent> events) {
        for (VFileEvent e : events) {
            if (e instanceof VFileDeleteEvent) {
                VirtualFile file = e.getFile();
                if (isNote(file)) {

                    String key = getNoteKey(file);

                    if (!notesToSkipChangeOrDeletedEvents.checkContainsAndRemove(key)) {
                        eventBus.post(new NoteFileDeletedEvent(key, file));
                    }
                }
            }
        }
    }

    @Override
    public void after(@NotNull List<? extends VFileEvent> events) {
        for (VFileEvent e : events) {
            VirtualFile file = e.getFile();
            if (file != null && isNote(file) && isChangeEvent(e)) {
                String key = getNoteKey(file);

                if (!notesToSkipChangeOrDeletedEvents.checkContainsAndRemove(key)) {
                    eventBus.post(new NoteFileChangedEvent(key, file));
                }
            }
        }
    }

    public String getNoteKey(VirtualFile file) {
        return file.getName();
    }

    private boolean isChangeEvent(VFileEvent e) {
        return e instanceof VFileCreateEvent || e instanceof VFileDeleteEvent || e instanceof VFileContentChangeEvent || e instanceof VFileCopyEvent || e instanceof VFileMoveEvent;
    }

    public @Nullable VirtualFile getSyncFile() {
        return virtualFileBy(SYNC_FILE);
    }

    public void getSyncFile(final Callback<VirtualFile> callback) {
        VirtualFile syncFile = virtualFileBy(SYNC_FILE);

        if (syncFile == null) {
            ApplicationManager.getApplication().invokeLater(new TrackedRunnable() {
                @Override
                public void doRun() {
                    callback.call(createSupportFile(SYNC_FILE, ""));
                }
            });
        } else {
            callback.call(syncFile);
        }
    }

    private VirtualFile createSupportFile(String fileName, String content) {
        boolean created = createFile(fileName, content, NOTES_FOLDER_PATH);

        if (!created) {
            throw new IllegalStateException(MessageBundle.message("exception.file.can_not_create", fileName, content));
        }

        return virtualFileBy(fileName);
    }

    //=========== SETTERS ============
    public void setEventBus(EventBus eventBus) {
        this.eventBus = eventBus;
    }
}
