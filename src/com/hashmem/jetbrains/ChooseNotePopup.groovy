/*
 * ChooseNotePopup
 * Copyright (c) 2012 Cybervision. All rights reserved.
 */
package com.hashmem.jetbrains
import com.intellij.ide.util.gotoByName.ChooseByNameItemProvider
import com.intellij.ide.util.gotoByName.ChooseByNameModel
import com.intellij.ide.util.gotoByName.ChooseByNamePopup
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Key
import com.intellij.psi.PsiElement
import org.jetbrains.annotations.NotNull
import org.jetbrains.annotations.Nullable

class ChooseNotePopup extends ChooseByNamePopup {

    public static final Key<ChooseByNamePopup> CHOOSE_BY_NAME_POPUP_IN_PROJECT_KEY = new Key<ChooseByNamePopup>("ChooseNotePopup");

    protected ChooseNotePopup(
            @Nullable Project project,
            @NotNull ChooseByNameModel model,
            @NotNull ChooseByNameItemProvider provider,
            @Nullable ChooseByNamePopup oldPopup, @Nullable String predefinedText, boolean mayRequestOpenInCurrentWindow, int initialIndex) {
        super(project, model, provider, oldPopup, predefinedText, mayRequestOpenInCurrentWindow, initialIndex)
    }

    public static ChooseNotePopup createPopup(final Project project, final ChooseByNameModel model, final PsiElement context,
                                                @Nullable final String predefinedText,
                                                boolean mayRequestOpenInCurrentWindow, final int initialIndex) {
        return createPopup(project, model, new HashMemItemProvider(context), predefinedText, mayRequestOpenInCurrentWindow,
                initialIndex);
    }

    public static ChooseNotePopup createPopup(final Project project,
                                                @NotNull ChooseByNameModel model,
                                                @NotNull ChooseByNameItemProvider provider,
                                                @Nullable final String predefinedText,
                                                boolean mayRequestOpenInCurrentWindow,
                                                final int initialIndex) {
        final ChooseNotePopup oldPopup = project == null ? null : project.getUserData(CHOOSE_BY_NAME_POPUP_IN_PROJECT_KEY);
        if (oldPopup != null) {
            oldPopup.close(false);
        }
        ChooseNotePopup newPopup = new ChooseNotePopup(project, model, provider, oldPopup, predefinedText, mayRequestOpenInCurrentWindow,
                initialIndex);

        if (project != null) {
            project.putUserData(CHOOSE_BY_NAME_POPUP_IN_PROJECT_KEY, newPopup);
        }
        return newPopup;
    }

    @Override
    public String transformPattern(String pattern) {
        if (pattern != null && pattern.length() > 0) {
            String first = pattern.charAt(0);

            if (first == "+" || first == "-" || first == "/") {
                return pattern.substring(1);
            }
        }

        return pattern;
    }
}
