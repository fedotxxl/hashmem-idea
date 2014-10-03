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

import com.intellij.CommonBundle;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.PathManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.util.Computable;
import com.intellij.openapi.util.Condition;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.util.Function;
import com.intellij.util.containers.ContainerUtil;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.List;

import static com.intellij.util.containers.ContainerUtil.findAll;
import static com.intellij.util.containers.ContainerUtil.map;


public class FileSystem {
	private static final Logger LOG = Logger.getInstance(FileSystem.class);

	/**
	 * Use UTF-8 to be compatible with old version of plugin.
	 */
	private static final Charset CHARSET = Charset.forName("UTF8");
	private static final String HASHMEM_FOLDER = "";

	private final VirtualFileManager fileManager = VirtualFileManager.getInstance();
	private final Condition<VirtualFile> canBeScratch = new Condition<VirtualFile>() {
		@Override public boolean value(VirtualFile it) {
			return it != null && it.exists() && !it.isDirectory() && !isHidden(it.getName());
		}
	};
	private final String scratchesFolderPath;


    public FileSystem() {
        this(null);
    }

	public FileSystem(String scratchesFolderPath) {
		if (scratchesFolderPath == null || scratchesFolderPath.isEmpty()) {
			this.scratchesFolderPath = PathManager.getPluginsPath() + "/hm/";
		} else {
			this.scratchesFolderPath = scratchesFolderPath + "/"; // add trailing "/" in case it's not specified in config
		}
	}

	public List<String> listFiles() {
		VirtualFile virtualFile = virtualFileBy(HASHMEM_FOLDER);
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
		return ApplicationManager.getApplication().runWriteAction(new Computable<Boolean>() {
			@Override public Boolean compute() {
				try {
					ensureExists(new File(scratchesFolderPath));

					VirtualFile scratchesFolder = virtualFileBy(HASHMEM_FOLDER);
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

	public boolean removeFile(final String fileName) {
		return ApplicationManager.getApplication().runWriteAction(new Computable<Boolean>() {
			@Override public Boolean compute() {
				VirtualFile virtualFile = virtualFileBy(fileName);
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

	@Nullable public VirtualFile virtualFileBy(String fileName) {
		return fileManager.refreshAndFindFileByUrl("file://" + scratchesFolderPath + fileName);
	}

	public boolean isScratch(final VirtualFile virtualFile) {
		VirtualFile scratchFolder = virtualFileBy(HASHMEM_FOLDER);
		return scratchFolder != null && ContainerUtil.exists(scratchFolder.getChildren(), new Condition<VirtualFile>() {
			@Override public boolean value(VirtualFile it) {
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
}
