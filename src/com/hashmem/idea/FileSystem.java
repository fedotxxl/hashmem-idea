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

import com.hashmem.idea.remote.SyncService;
import com.intellij.CommonBundle;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.PathManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.util.Computable;
import com.intellij.openapi.util.Condition;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.openapi.vfs.newvfs.BulkFileListener;
import com.intellij.openapi.vfs.newvfs.events.VFileDeleteEvent;
import com.intellij.openapi.vfs.newvfs.events.VFileEvent;
import com.intellij.util.Function;
import com.intellij.util.containers.ContainerUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import static com.intellij.util.containers.ContainerUtil.findAll;
import static com.intellij.util.containers.ContainerUtil.map;


public class FileSystem implements BulkFileListener, Startable {
	private static final Logger LOG = Logger.getInstance(FileSystem.class);

    private SettingsService settingsService;
    private SyncService syncService;

	/**
	 * Use UTF-8 to be compatible with old version of plugin.
	 */
	private static final Charset CHARSET = Charset.forName("UTF8");
	private static final String HASHMEM_FOLDER = "";
	private static final String SETTINGS_FILE = ".settings";

	private final VirtualFileManager fileManager = VirtualFileManager.getInstance();
	private final Condition<VirtualFile> canBeScratch = new Condition<VirtualFile>() {
		@Override public boolean value(VirtualFile it) {
			return it != null && it.exists() && !it.isDirectory() && !isHidden(it.getName());
		}
	};
	private final String scratchesFolderPath;
	private final String removedFolderPath;


    public FileSystem() {
        scratchesFolderPath = PathManager.getPluginsPath() + "/hm/";
        removedFolderPath = PathManager.getPluginsPath() + "/hm.removed/";
    }

    private VirtualFile getRootFolder() {
        return virtualFileBy(HASHMEM_FOLDER);
    }

	public List<String> listFiles() {
		VirtualFile virtualFile = getRootFolder();
		if (virtualFile == null || !virtualFile.exists()) {
			return Collections.emptyList();
		}
		return map(findAll(virtualFile.getChildren(), canBeScratch), new Function<VirtualFile, String>() {
			@Override public String fun(VirtualFile it) {
				return it.getName();
			}
		});
	}

	public boolean scratchFileExists(String fileName) {
		VirtualFile virtualFile = virtualFileBy(fileName);
		return canBeScratch.value(virtualFile);
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
        return createFile(fileName, text, scratchesFolderPath);
    }

    private boolean createFile(final String fileName, final String text, final String path) {
        return ApplicationManager.getApplication().runReadAction(new Computable<Boolean>() {

            @Override
            public Boolean compute() {
                return ApplicationManager.getApplication().runWriteAction(new Computable<Boolean>() {
                    @Override public Boolean compute() {
                        try {
                            ensureExists(new File(path));

                            VirtualFile scratchesFolder = virtualFileBy(path, true);
                            if (scratchesFolder == null) return false;

                            VirtualFile scratchFile = scratchesFolder.createChildData(FileSystem.this, fileName);
                            scratchFile.setBinaryContent(text.getBytes(CHARSET));

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

	public boolean removeFile(final String fileName) {
		return ApplicationManager.getApplication().runWriteAction(new Computable<Boolean>() {
			@Override public Boolean compute() {
				VirtualFile virtualFile = virtualFileBy(fileName);
				if (virtualFile == null) return false;

				try {
                    markAsDeleted(virtualFile);
					virtualFile.delete(FileSystem.this);
					return true;
				} catch (IOException e) {
					LOG.warn(e);
					return false;
				}
			}
		});
	}

    @Nullable public VirtualFile virtualFileBy(String fileName) {
        return virtualFileBy(fileName, false);
    }

	@Nullable private VirtualFile virtualFileBy(String fileName, boolean isAbsolute) {
		return fileManager.refreshAndFindFileByUrl("file://" + ((isAbsolute) ? fileName : (scratchesFolderPath + fileName)));
	}

	public boolean isHashMemNote(final VirtualFile virtualFile) {
		VirtualFile scratchFolder = getRootFolder();
		return scratchFolder != null && ContainerUtil.exists(scratchFolder.getChildren(), new Condition<VirtualFile>() {
			@Override public boolean value(VirtualFile it) {
				return it.equals(virtualFile);
			}
		});
	}

    public Collection<VirtualFile> getNotesChangesSince(long since) {
        return getFilesChangedInFolder(scratchesFolderPath, since);
    }

    public Collection<VirtualFile> getNotesDeletedSince(long since) {
        return getFilesChangedInFolder(removedFolderPath, since);
    }

    private Collection<VirtualFile> getFilesChangedInFolder(String path, long since) {
        Collection<VirtualFile> answer = new HashSet<VirtualFile>();
        VirtualFile deletedFolder = virtualFileBy(path, true);

        if (deletedFolder != null) {
            for (VirtualFile file : deletedFolder.getChildren()) {
                if (!file.isDirectory()) {
                    if (getLastModified(file) > since && !isHidden(file.getName())) answer.add(file);
                }
            }
        }

        return answer;
    }

    public static long getLastModified(VirtualFile file) {
        return new File(file.getCanonicalPath()).lastModified();
    }

    private void markAsDeleted(VirtualFile file) {
        createFile(file.getName(), "", removedFolderPath);
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
                VFileDeleteEvent event = (VFileDeleteEvent) e;
                if (isHashMemNote(e.getFile())) {
                    markAsDeleted(e.getFile());
                    syncService.syncLater();
                }
            }
        }
    }

    @Override
    public void after(@NotNull List<? extends VFileEvent> events) {
        for (VFileEvent e : events) {
            VirtualFile file = e.getFile();
            if (isSettingsFile(file)) {
                refreshSettings(file);
            } else if (isHashMemNote(file)) {
                syncService.syncLater();
            }
        }
    }

    //settings
    private void refreshSettings(VirtualFile settings) {
        if (settings == null) {
            createDefaultSettingsFile();
        } else {
            settingsService.refresh(settings);
        }
    }

    public VirtualFile getSettingsFile() {
        VirtualFile settings = virtualFileBy(SETTINGS_FILE);

        if (settings == null) {
            createDefaultSettingsFile();
            settings = virtualFileBy(SETTINGS_FILE);
        }

        return settings;
    }

    private void createDefaultSettingsFile() {
        boolean created = createFile(SETTINGS_FILE, SettingsService.DEFAULT_CONTENT);

        if (!created) {
            throw new IllegalStateException("Unable to create settings file");
        }
    }

    private boolean isSettingsFile(VirtualFile file) {
        return SETTINGS_FILE.equals(file.getName()) && file.getParent().equals(getRootFolder());
    }

    @Override
    public void postConstruct() {
        refreshSettings(getSettingsFile());
    }

    //=========== SETTERS ============
    public void setSettingsService(SettingsService settingsService) {
        this.settingsService = settingsService;
    }

    public void setSyncService(SyncService syncService) {
        this.syncService = syncService;
    }
}
