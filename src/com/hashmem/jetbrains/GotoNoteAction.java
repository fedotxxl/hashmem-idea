/*
 * GotoNoteAction
 * Copyright (c) 2012 Cybervision. All rights reserved.
 */
package com.hashmem.jetbrains;

import com.hashmem.jetbrains.command.HashMemCommand;
import com.intellij.featureStatistics.FeatureUsageTracker;
import com.intellij.ide.actions.GotoActionBase;
import com.intellij.ide.util.gotoByName.*;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ModalityState;
import com.intellij.openapi.fileEditor.OpenFileDescriptor;
import com.intellij.openapi.fileEditor.ex.FileEditorManagerEx;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.fileTypes.FileTypeManager;
import com.intellij.openapi.fileTypes.FileTypes;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.pom.Navigatable;
import com.intellij.psi.PsiFile;
import com.intellij.util.containers.ContainerUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class GotoNoteAction extends GotoActionBase implements DumbAware {
    public static final String ID = "GotoFile";

    @Override
    public void gotoActionPerformed(AnActionEvent e) {
        FeatureUsageTracker.getInstance().triggerFeatureUsed("navigation.popup.file");
        final Project project = e.getData(CommonDataKeys.PROJECT);
        final GotoFileModel gotoFileModel = new HashMemFileModel(project);
        showNavigationPopup(e, gotoFileModel, new GotoActionCallback<FileType>() {
            @Override
            protected ChooseByNameFilter<FileType> createFilter(@NotNull ChooseByNamePopup popup) {
                return new GotoFileFilter(popup, gotoFileModel, project);
            }

            @Override
            public void elementChosen(final ChooseByNamePopup popup, final Object element) {
                if (element == null) return;
                ApplicationManager.getApplication().invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        Navigatable n = (Navigatable)element;

                        //this is for better cursor position
                        if (element instanceof PsiFile) {
                            VirtualFile vfile = ((PsiFile)element).getVirtualFile();
                            if (vfile == null) return;
                            n = new OpenFileDescriptor(project, vfile, popup.getLinePosition(), popup.getColumnPosition()).setUseCurrentWindow(popup.isOpenInCurrentWindowRequested());
                        }

                        if (element instanceof HashMemCommand) {
                            ((HashMemCommand) element).process();
                        }

                        if (!n.canNavigate()) return;
                        n.navigate(true);
                    }
                }, ModalityState.NON_MODAL);
            }
        }, "Files matching pattern", true);
    }

    protected static class GotoFileFilter extends ChooseByNameFilter<FileType> {
        GotoFileFilter(final ChooseByNamePopup popup, GotoFileModel model, final Project project) {
            super(popup, model, GotoFileConfiguration.getInstance(project), project);
        }

        @Override
        @NotNull
        protected List<FileType> getAllFilterValues() {
            List<FileType> elements = new ArrayList<FileType>();
            ContainerUtil.addAll(elements, FileTypeManager.getInstance().getRegisteredFileTypes());
            Collections.sort(elements, FileTypeComparator.INSTANCE);
            return elements;
        }

        @Override
        protected String textForFilterValue(@NotNull FileType value) {
            return value.getName();
        }

        @Override
        protected Icon iconForFilterValue(@NotNull FileType value) {
            return value.getIcon();
        }
    }

    /**
     * A file type comparator. The comparison rules are applied in the following order.
     * <ol>
     * <li>Unknown file type is greatest.</li>
     * <li>Text files are less then binary ones.</li>
     * <li>File type with greater name is greater (case is ignored).</li>
     * </ol>
     */
    static class FileTypeComparator implements Comparator<FileType> {
        /**
         * an instance of comparator
         */
        static final Comparator<FileType> INSTANCE = new FileTypeComparator();

        /**
         * {@inheritDoc}
         */
        @Override
        public int compare(final FileType o1, final FileType o2) {
            if (o1 == o2) {
                return 0;
            }
            if (o1 == FileTypes.UNKNOWN) {
                return 1;
            }
            if (o2 == FileTypes.UNKNOWN) {
                return -1;
            }
            if (o1.isBinary() && !o2.isBinary()) {
                return 1;
            }
            if (!o1.isBinary() && o2.isBinary()) {
                return -1;
            }
            return o1.getName().compareToIgnoreCase(o2.getName());
        }
    }

    protected <T> void showNavigationPopup(AnActionEvent e,
                                           ChooseByNameModel model,
                                           final GotoActionCallback<T> callback,
                                           @Nullable final String findUsagesTitle,
                                           boolean useSelectionFromEditor,
                                           final boolean allowMultipleSelection) {
        final Project project = e.getData(CommonDataKeys.PROJECT);
        boolean mayRequestOpenInCurrentWindow = model.willOpenEditor() && FileEditorManagerEx.getInstanceEx(project).hasSplitOrUndockedWindows();
        Pair<String, Integer> start = getInitialText(useSelectionFromEditor, e);
        showNavigationPopup(callback, findUsagesTitle,
                ChooseNotePopup.createPopup(project, model, getPsiContext(e), start.first,
                        mayRequestOpenInCurrentWindow, start.second), allowMultipleSelection);
    }
}
