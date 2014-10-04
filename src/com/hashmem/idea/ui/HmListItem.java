/*
 * HmNote
 * Copyright (c) 2012 Cybervision. All rights reserved.
 */
package com.hashmem.idea.ui;

import com.hashmem.idea.Note;
import com.hashmem.jetbrains.command.HashMemCommandPresentation;
import com.intellij.navigation.ItemPresentation;
import com.intellij.navigation.NavigationItem;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class HmListItem implements NavigationItem {

    private Note note;
    private String prefix;

    public HmListItem(@NotNull String prefix, Note note) {
        this.prefix = prefix;
        this.note = note;
    }

    @Nullable
    @Override
    public String getName() {
        return prefix + note.getKey();
    }

    @Nullable
    @Override
    public ItemPresentation getPresentation() {
        return HashMemCommandPresentation.INSTANCE;
    }

    @Override
    public void navigate(boolean requestFocus) {

    }

    @Override
    public boolean canNavigate() {
        return false;
    }

    @Override
    public boolean canNavigateToSource() {
        return false;
    }

    @Override
    public String toString() {
        return getName();
    }

    public Note getNote() {
        return note;
    }

    public Query toQuery() {
        return new Query(prefix, note.getKey());
    }
}
