/*
 * HmPopup
 * Copyright (c) 2012 Cybervision. All rights reserved.
 */
package com.hashmem.idea;

import com.intellij.codeInsight.daemon.DaemonCodeAnalyzer;
import com.intellij.ide.util.gotoByName.GotoFileCellRenderer;
import com.intellij.ide.util.gotoByName.ModelDiff;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.MnemonicHelper;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.keymap.KeymapManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.popup.ComponentPopupBuilder;
import com.intellij.openapi.ui.popup.JBPopup;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.openapi.util.Computable;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.util.SystemInfo;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.wm.WindowManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiManager;
import com.intellij.psi.util.PsiUtilCore;
import com.intellij.ui.ScreenUtil;
import com.intellij.ui.ScrollPaneFactory;
import com.intellij.ui.components.JBList;
import com.intellij.ui.popup.PopupOwner;
import com.intellij.ui.popup.PopupPositionManager;
import com.intellij.ui.popup.PopupUpdateProcessor;
import com.intellij.util.ui.GraphicsUtil;
import com.intellij.util.ui.UIUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.PlainDocument;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.ArrayList;

public class HmPopup {

    protected JBPopup myDropdownPopup;
    protected JScrollPane myListScrollPane;
    private final MyListModel<Object> myListModel = new MyListModel<Object>();
    protected final JList myList = new JBList(myListModel);
    protected final Project myProject;
    protected JBPopup myTextPopup;
    protected final JPanelProvider myTextFieldPanel = new JPanelProvider();// Located in the layered pane
    private static int VISIBLE_LIST_SIZE_LIMIT = 10;



    //    protected final JPanelProvider myTextFieldPanel = new JPanelProvider();// Located in the layered pane
    protected final MyTextField myTextField = new MyTextField();


    public HmPopup(Project myProject) {
        this.myProject = myProject;
        myList.setCellRenderer(new GotoFileCellRenderer(15));
    }

    void show() {

        myListModel.addToModel(0, "Hello world");
        for (VirtualFile file : myProject.getBaseDir().getChildren()) {
            myListModel.addElement(PsiManager.getInstance(myProject).findFile(file));
        }

        myTextFieldPanel.setLayout(new BoxLayout(myTextFieldPanel, BoxLayout.Y_AXIS));
        myTextFieldPanel.add(myTextField);
        myTextFieldPanel.setBorder(new EmptyBorder(2, 2, 2, 2));
        showTextFieldPanel();


        myListScrollPane = ScrollPaneFactory.createScrollPane(myList);
        myListScrollPane.setViewportBorder(new EmptyBorder(0, 0, 0, 0));

        Rectangle bounds = new Rectangle(myTextFieldPanel.getLocationOnScreen(), myTextField.getSize());
        bounds.y += myTextFieldPanel.getHeight() + (SystemInfo.isMac ? 3 : 1);

        final Dimension preferredScrollPaneSize = myListScrollPane.getPreferredSize();
        if (myList.getModel().getSize() == 0) {
            preferredScrollPaneSize.height = UIManager.getFont("Label.font").getSize();
        }


        Rectangle preferredBounds = new Rectangle(bounds.x, bounds.y, bounds.width, preferredScrollPaneSize.height);
        Rectangle original = new Rectangle(preferredBounds);

        ScreenUtil.fitToScreen(preferredBounds);
        if (original.width > preferredBounds.width) {
            int height = myListScrollPane.getHorizontalScrollBar().getPreferredSize().height;
            preferredBounds.height += height;
        }

        if (myDropdownPopup == null) {
            final JLayeredPane layeredPane = myTextField.getRootPane().getLayeredPane();

            ComponentPopupBuilder builder = JBPopupFactory.getInstance().createComponentPopupBuilder(myListScrollPane, myListScrollPane);
            builder.setFocusable(false)
                    .setRequestFocus(false)
                    .setCancelKeyEnabled(false)
//                    .setFocusOwners(new JComponent[]{myTextField})
                    .setBelongsToGlobalPopupStack(false)
                    .setModalContext(false)
//                    .setAdText(adText)
                    .setMayBeParent(true);
            builder.setCancelCallback(new Computable<Boolean>() {
                @Override
                public Boolean compute() {
                    return Boolean.TRUE;
                }
            });
            myDropdownPopup = builder.createPopup();
            myDropdownPopup.setLocation(preferredBounds.getLocation());
            myDropdownPopup.setSize(preferredBounds.getSize());
            myDropdownPopup.show(layeredPane);
            myList.updateUI();
        }
    }

    protected void showTextFieldPanel() {
        final JLayeredPane layeredPane = getLayeredPane();
        final Dimension preferredTextFieldPanelSize = myTextFieldPanel.getPreferredSize();
        final int x = (layeredPane.getWidth() - preferredTextFieldPanelSize.width) / 2;
        final int paneHeight = layeredPane.getHeight();
        final int y = paneHeight / 3 - preferredTextFieldPanelSize.height / 2;

        VISIBLE_LIST_SIZE_LIMIT = Math.max
                (10, (paneHeight - (y + preferredTextFieldPanelSize.height)) / (preferredTextFieldPanelSize.height / 2) - 1);

        ComponentPopupBuilder builder = JBPopupFactory.getInstance().createComponentPopupBuilder(myTextFieldPanel, myTextField);
        builder.setFocusable(true).setRequestFocus(true).setModalContext(false).setCancelOnClickOutside(false);

        Point point = new Point(x, y);
        SwingUtilities.convertPointToScreen(point, layeredPane);
        Rectangle bounds = new Rectangle(point, new Dimension(preferredTextFieldPanelSize.width + 20, preferredTextFieldPanelSize.height));
        myTextPopup = builder.createPopup();
        myTextPopup.setSize(bounds.getSize());
        myTextPopup.setLocation(bounds.getLocation());

        new MnemonicHelper().register(myTextFieldPanel);
        if (myProject != null && !myProject.isDefault()) {
            DaemonCodeAnalyzer.getInstance(myProject).disableUpdateByTimer(myTextPopup);
        }

        Disposer.register(myTextPopup, new Disposable() {
            @Override
            public void dispose() {
            }
        });
        myTextPopup.show(layeredPane);
    }

    private JLayeredPane getLayeredPane() {
        JLayeredPane layeredPane;
        final Window window = WindowManager.getInstance().suggestParentWindow(myProject);

        Component parent = UIUtil.findUltimateParent(window);

        if (parent instanceof JFrame) {
            layeredPane = ((JFrame)parent).getLayeredPane();
        }
        else if (parent instanceof JDialog) {
            layeredPane = ((JDialog)parent).getLayeredPane();
        }
        else {
            throw new IllegalStateException("cannot find parent window: project=" + myProject +
                    (myProject != null ? "; open=" + myProject.isOpen() : "") +
                    "; window=" + window);
        }
        return layeredPane;
    }

    private static class MyListModel<T> extends DefaultListModel implements ModelDiff.Model<T> {
        @Override
        public void addToModel(int idx, T element) {
            if (idx < size()) {
                add(idx, element);
            }
            else {
                addElement(element);
            }
        }

        @Override
        public void removeRangeFromModel(int start, int end) {
            if (start < size() && size() != 0) {
                removeRange(start, Math.min(end, size()-1));
            }
        }
    }

    public final class MyTextField extends JTextField implements PopupOwner, TypeSafeDataProvider {
        private final KeyStroke myCompletionKeyStroke;
        private final KeyStroke forwardStroke;
        private final KeyStroke backStroke;

        private boolean completionKeyStrokeHappened = false;

        private MyTextField() {
            super(40);
            enableEvents(AWTEvent.KEY_EVENT_MASK);
            myCompletionKeyStroke = getShortcut(IdeActions.ACTION_CODE_COMPLETION);
            forwardStroke = getShortcut(IdeActions.ACTION_GOTO_FORWARD);
            backStroke = getShortcut(IdeActions.ACTION_GOTO_BACK);
            setFocusTraversalKeysEnabled(false);
            putClientProperty("JTextField.variant", "search");
            setDocument(new PlainDocument() {
                @Override
                public void insertString(int offs, String str, AttributeSet a) throws BadLocationException {
                    super.insertString(offs, str, a);
                    if (str != null && str.length() > 1) {
//                        handlePaste(str);
                    }
                }
            });
        }

        @Override
        public Dimension getPreferredSize() {
            Dimension size = super.getPreferredSize();
            Border border = super.getBorder();
            if (border != null && UIUtil.isUnderAquaLookAndFeel()) {
                Insets insets = border.getBorderInsets(this);
                size.height += insets.top + insets.bottom;
                size.width += insets.left + insets.right;
            }
            return size;
        }

        @Nullable
        private KeyStroke getShortcut(String actionCodeCompletion) {
            final Shortcut[] shortcuts = KeymapManager.getInstance().getActiveKeymap().getShortcuts(actionCodeCompletion);
            for (final Shortcut shortcut : shortcuts) {
                if (shortcut instanceof KeyboardShortcut) {
                    return ((KeyboardShortcut)shortcut).getFirstKeyStroke();
                }
            }
            return null;
        }

        @Override
        public void calcData(final DataKey key, @NotNull final DataSink sink) {
            if (LangDataKeys.POSITION_ADJUSTER_POPUP.equals(key)) {
                if (myDropdownPopup != null && myDropdownPopup.isVisible()) {
                    sink.put(key, myDropdownPopup);
                }
            }
            else if (LangDataKeys.PARENT_POPUP.equals(key)) {
            }
        }

        @Override
        protected void processKeyEvent(@NotNull KeyEvent e) {

        }

        private void fillInCommonPrefix(@NotNull final String pattern) {

        }

        private boolean isComplexPattern(@NotNull final String pattern) {
           return false;
        }

        @Override
        @Nullable
        public Point getBestPopupPosition() {
            return new Point(10, 10);
        }

        @Override
        protected void paintComponent(@NotNull final Graphics g) {
            GraphicsUtil.setupAntialiasing(g);
            super.paintComponent(g);
        }

        public boolean isCompletionKeyStroke() {
            return completionKeyStrokeHappened;
        }
    }

    public class JPanelProvider extends JPanel implements DataProvider {
        private JBPopup myHint = null;
        private boolean myFocusRequested = false;

        JPanelProvider() {
        }

        @Override
        public Object getData(String dataId) {
            if (!false) {
                return null;
            }
            if (CommonDataKeys.PSI_ELEMENT.is(dataId)) {
                Object element = new Object();

                if (element instanceof PsiElement) {
                    return element;
                }

                if (element instanceof DataProvider) {
                    return ((DataProvider)element).getData(dataId);
                }
            }
            else if (LangDataKeys.PSI_ELEMENT_ARRAY.is(dataId)) {
                final java.util.List<Object> chosenElements = null;
                if (chosenElements != null) {
                    java.util.List<PsiElement> result = new ArrayList<PsiElement>(chosenElements.size());
                    for (Object element : chosenElements) {
                        if (element instanceof PsiElement) {
                            result.add((PsiElement)element);
                        }
                    }
                    return PsiUtilCore.toPsiElementArray(result);
                }
            }
            else if (PlatformDataKeys.DOMINANT_HINT_AREA_RECTANGLE.is(dataId)) {
                return getBounds();
            }
            return null;
        }

        public void registerHint(JBPopup h) {
            if (myHint != null && myHint.isVisible() && myHint != h) {
                myHint.cancel();
            }
            myHint = h;
        }

        public boolean focusRequested() {
            boolean focusRequested = myFocusRequested;

            myFocusRequested = false;

            return focusRequested;
        }

        @Override
        public void requestFocus() {
            myFocusRequested = true;
        }

        public void unregisterHint() {
            myHint = null;
        }

        public void hideHint() {
            if (myHint != null) {
                myHint.cancel();
            }
        }

        @Nullable
        public JBPopup getHint() {
            return myHint;
        }

        public void updateHint(PsiElement element) {
            if (myHint == null || !myHint.isVisible()) return;
            final PopupUpdateProcessor updateProcessor = myHint.getUserData(PopupUpdateProcessor.class);
            if (updateProcessor != null) {
                updateProcessor.updatePopup(element);
            }
        }

        public void repositionHint() {
            if (myHint == null || !myHint.isVisible()) return;
            PopupPositionManager.positionPopupInBestPosition(myHint, null, null);
        }
    }
}
