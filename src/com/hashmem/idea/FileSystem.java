/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.hashmem.idea;

import com.google.common.eventbus.EventBus;
import com.hashmem.idea.event.NoteFileChangedEvent;
import com.hashmem.idea.event.NoteFileDeletedEvent;
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

    private final String notesFolderPath;
    private final VirtualFileManager fileManager = VirtualFileManager.getInstance();
	private final Condition<VirtualFile> canBeNote = new Condition<VirtualFile>() {
		@Override public boolean value(VirtualFile it) {
			return it != null && it.exists() && !it.isDirectory() && !isHidden(it.getName());
		}
	};
    private OneTimeContainer<String> notesToSkipChangeOrDeletedEvents = new OneTimeContainer<String>();

    public FileSystem() {
        notesFolderPath = PathManager.getPluginsPath() + "/hm/";
    }

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

	public boolean noteFileExists(String fileName) {
		return canBeNote.value(virtualFileBy(fileName));
	}

	public boolean renameFile(String oldFileName, final String newFileName) {
		final VirtualFile virtualFile = virtualFileBy(oldFileName);
		if (virtualFile == null) return false;

		return ApplicationManager.getApplication().runWriteAction(new Computable<Boolean>() {
			@Override public Boolean compute() {
				try {
					virtualFile.rename(this, newFileName);
					return true;
				} catch (IOException e) {
					LOG.warn(e);
					return false;
				}
			}
		});
	}

	public boolean createEmptyFile(String fileName) {
		return createFile(fileName, "");
	}

    public boolean createFile(final String fileName, final String text) {
        return createFile(fileName, text, notesFolderPath);
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
        return notesFolderPath + fileName;
    }

	public boolean isHashMemNote(final VirtualFile virtualFile) {
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
                if (isHashMemNote(file)) {

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
            if (isHashMemNote(file) && isChangeEvent(e)) {
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

    public VirtualFile getSyncFile() {
        VirtualFile deleted = virtualFileBy(SYNC_FILE);

        if (deleted == null) {
            deleted = createSupportFile(SYNC_FILE, "");
        }

        return deleted;
    }

    private VirtualFile createSupportFile(String fileName, String content) {
        boolean created = createFile(fileName, content);

        if (!created) {
            throw new IllegalStateException("Unable to create file " + fileName + " with content " + content);
        }

        return virtualFileBy(fileName);
    }

    //=========== SETTERS ============
    public void setEventBus(EventBus eventBus) {
        this.eventBus = eventBus;
    }
}
