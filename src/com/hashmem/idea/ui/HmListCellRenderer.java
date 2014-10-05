/*
 * HmListCellRenderer
 * Copyright (c) 2012 Cybervision. All rights reserved.
 */
package com.hashmem.idea.ui;

import com.intellij.ide.util.gotoByName.GotoFileCellRenderer;
import com.intellij.ui.ColoredListCellRenderer;
import com.intellij.ui.SimpleTextAttributes;
import com.intellij.ui.speedSearch.SpeedSearchUtil;
import com.intellij.util.text.Matcher;
import com.intellij.util.ui.UIUtil;

import javax.swing.*;
import java.awt.*;

public class HmListCellRenderer extends GotoFileCellRenderer {
    private Matcher myMatcher;

    public HmListCellRenderer(int maxSize) {
        super(maxSize);
    }

    @Override
    public void setPatternMatcher(final Matcher matcher) {
        myMatcher = matcher;
        super.setPatternMatcher(matcher);
    }

    @Override
    protected boolean customizeNonPsiElementLeftRenderer(ColoredListCellRenderer renderer, JList list, Object value, int index, boolean selected, boolean hasFocus) {
        if (value instanceof HmListItem) {
            Color bgColor = UIUtil.getListBackground();
            Color color = list.getForeground();

            HmListItem item = (HmListItem) value;

            SimpleTextAttributes nameAttributes = new SimpleTextAttributes(Font.PLAIN, color);

            SpeedSearchUtil.appendColoredFragmentForMatcher(item.getName(), renderer, nameAttributes, myMatcher, bgColor, selected);

            return true;
        } else {
            return super.customizeNonPsiElementLeftRenderer(renderer, list, value, index, selected, hasFocus);
        }
    }
}
