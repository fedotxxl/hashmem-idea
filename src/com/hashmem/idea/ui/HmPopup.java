/*
 * HmPopup
 * Copyright (c) 2012 Cybervision. All rights reserved.
 */
package com.hashmem.idea.ui;

import com.google.common.collect.Lists;
import com.hashmem.idea.domain.Command;
import com.hashmem.idea.domain.Note;
import com.hashmem.idea.domain.Query;
import com.hashmem.idea.service.ActionProcessor;
import com.hashmem.idea.service.NotesService;
import com.hashmem.idea.tracked.TrackedActionListener;
import com.hashmem.idea.tracked.TrackedDocumentAdapter;
import com.hashmem.idea.utils.ExceptionTracker;
import com.intellij.codeInsight.daemon.DaemonCodeAnalyzer;
import com.intellij.ide.actions.GotoActionBase;
import com.intellij.ide.util.gotoByName.ChooseByNameItemProvider;
import com.intellij.ide.util.gotoByName.MatchResult;
import com.intellij.ide.util.gotoByName.ModelDiff;
import com.intellij.openapi.MnemonicHelper;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.keymap.Keymap;
import com.intellij.openapi.keymap.KeymapManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.popup.ComponentPopupBuilder;
import com.intellij.openapi.ui.popup.JBPopup;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.openapi.util.Computable;
import com.intellij.openapi.util.SystemInfo;
import com.intellij.openapi.wm.WindowManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.codeStyle.MinusculeMatcher;
import com.intellij.psi.codeStyle.NameUtil;
import com.intellij.psi.util.PsiUtilCore;
import com.intellij.ui.JBColor;
import com.intellij.ui.ListScrollingUtil;
import com.intellij.ui.ScreenUtil;
import com.intellij.ui.ScrollPaneFactory;
import com.intellij.ui.components.JBList;
import com.intellij.ui.popup.PopupOwner;
import com.intellij.ui.popup.PopupPositionManager;
import com.intellij.ui.popup.PopupUpdateProcessor;
import com.intellij.util.text.Matcher;
import com.intellij.util.text.MatcherHolder;
import com.intellij.util.ui.GraphicsUtil;
import com.intellij.util.ui.UIUtil;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.PlainDocument;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class HmPopup {

    private static int VISIBLE_LIST_SIZE_LIMIT = 6;
    private static int LIST_SIZE_LIMIT = VISIBLE_LIST_SIZE_LIMIT;

    protected JBPopup myDropdownPopup;
    protected JScrollPane myListScrollPane;
    private final MyListModel<HmListItem> myListModel = new MyListModel<HmListItem>();
    protected final JList myList = new JBList(myListModel);
    protected final Project myProject;
    protected JBPopup myTextPopup;
    protected final JPanelProvider myTextFieldPanel = new JPanelProvider();// Located in the layered pane
    private ListCellRenderer listCellRenderer = new HmListCellRenderer(15);
    protected ChooseByNameItemProvider myProvider;
    private NotesService notesService;
    private ActionProcessor actionProcessor;
    private Query currentQuery;
    private boolean isSkipQueryChangeEvent = false;

    //    protected final JPanelProvider myTextFieldPanel = new JPanelProvider();// Located in the layered pane
    protected final MyTextField myTextField = new MyTextField();


    public HmPopup(Project myProject, NotesService notesService, ActionProcessor actionProcessor) {
        this.myProject = myProject;
        this.myProvider = new HmItemProvider(GotoActionBase.getPsiContext(myProject));
        this.notesService = notesService;
        this.actionProcessor = actionProcessor;
        myList.setCellRenderer(listCellRenderer);
    }

    void show() {
//        setMatcher("bOd");
//
//        myListModel.addToModel(0, "Hello world");
//        for (VirtualFile file : myProject.getBaseDir().getChildren()) {
//            myListModel.addElement(PsiManager.getInstance(myProject).findFile(file));
//        }

        myTextFieldPanel.setLayout(new BoxLayout(myTextFieldPanel, BoxLayout.Y_AXIS));
        myTextFieldPanel.add(myTextField);
        myTextFieldPanel.setBorder(new EmptyBorder(2, 2, 2, 2));
        showTextFieldPanel();

        myListScrollPane = ScrollPaneFactory.createScrollPane(myList);
        myListScrollPane.setViewportBorder(new EmptyBorder(0, 0, 0, 0));

        showList();
    }

    private void showList() {
        myList.setVisibleRowCount(Math.min(VISIBLE_LIST_SIZE_LIMIT, myList.getModel().getSize()));

        Rectangle bounds = new Rectangle(myTextFieldPanel.getLocationOnScreen(), myTextField.getSize());
        bounds.y += myTextFieldPanel.getHeight() + (SystemInfo.isMac ? 3 : 1);

        final Dimension preferredScrollPaneSize = myListScrollPane.getPreferredSize();
        if (myList.getModel().getSize() == 0) {
            preferredScrollPaneSize.height = UIManager.getFont("Label.font").getSize() + 8;
        }

        preferredScrollPaneSize.width = Math.max(myTextFieldPanel.getWidth(), preferredScrollPaneSize.width);

        Rectangle preferredBounds = new Rectangle(bounds.x, bounds.y, preferredScrollPaneSize.width, preferredScrollPaneSize.height);
        Rectangle original = new Rectangle(preferredBounds);

        ScreenUtil.fitToScreen(preferredBounds);
        if (original.width > preferredBounds.width) {
            int height = myListScrollPane.getHorizontalScrollBar().getPreferredSize().height;
            preferredBounds.height += height;
        }

        myListScrollPane.setVisible(true);
        myListScrollPane.setBorder(null);

        if (myDropdownPopup == null) {
            final JLayeredPane layeredPane = myTextField.getRootPane().getLayeredPane();

            ComponentPopupBuilder builder = JBPopupFactory.getInstance().createComponentPopupBuilder(myListScrollPane, myListScrollPane);
            builder.setFocusable(false)
                    .setRequestFocus(false)
                    .setCancelKeyEnabled(false)
                    .setFocusOwners(new JComponent[]{myTextField})
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
            myDropdownPopup.show(layeredPane);
            myList.setFocusable(false);
            myList.setSelectedIndex(5);
            myList.updateUI();

            myList.addListSelectionListener(new ListSelectionListener() {
                @Override
                public void valueChanged(ListSelectionEvent e) {
                    markAsValid();
                }
            });
        }

        myDropdownPopup.setSize(preferredBounds.getSize());
    }

    private void setMatcher(String pattern) {
        final Matcher matcher = buildPatternMatcher(isSearchInAnyPlace() ? "*" + pattern : pattern);
        ((MatcherHolder) listCellRenderer).setPatternMatcher(matcher);
    }

    private boolean isSearchInAnyPlace() {
        return true;
    }

    private static Matcher buildPatternMatcher(@NotNull String pattern) {
        return NameUtil.buildMatcher(pattern, 0, true, true, pattern.toLowerCase().equals(pattern));
    }

    @NotNull
    private static MinusculeMatcher buildPatternMatcher(@NotNull String pattern, @NotNull NameUtil.MatchingCaseSensitivity caseSensitivity) {
        return NameUtil.buildMatcher(pattern, caseSensitivity);
    }

    @Nullable
    private static MatchResult matches(@NotNull MinusculeMatcher matcher, @Nullable String name) {
        if (name == null) {
            return null;
        }

        return matcher.matches(name) ? new MatchResult(name, matcher.matchingDegree(name), matcher.isStartMatch(name)) : null;
    }

    protected void showTextFieldPanel() {
        final JLayeredPane layeredPane = getLayeredPane();
        final Dimension preferredTextFieldPanelSize = myTextFieldPanel.getPreferredSize();
        final int x = (layeredPane.getWidth() - preferredTextFieldPanelSize.width) / 2;
        final int paneHeight = layeredPane.getHeight();
        final int y = paneHeight / 3 - preferredTextFieldPanelSize.height / 2;

//        VISIBLE_LIST_SIZE_LIMIT = Math.max
//                (10, (paneHeight - (y + preferredTextFieldPanelSize.height)) / (preferredTextFieldPanelSize.height / 2) - 1);

        ComponentPopupBuilder builder = JBPopupFactory.getInstance().createComponentPopupBuilder(myTextFieldPanel, myTextField);
        builder.setCancelCallback(new Computable<Boolean>() {
            @Override
            public Boolean compute() {
                try {
                    myTextPopup = null;
                    close(false);
                } catch (Throwable t) {
                    ExceptionTracker.getInstance().trackAndRethrow(t);
                }

                return Boolean.TRUE;
            }
        }).setFocusable(true).setRequestFocus(true).setModalContext(false).setCancelOnClickOutside(false);

        Point point = new Point(x, y);
        SwingUtilities.convertPointToScreen(point, layeredPane);
        Rectangle bounds = new Rectangle(point, new Dimension(preferredTextFieldPanelSize.width + 20, preferredTextFieldPanelSize.height));
        myTextPopup = builder.createPopup();
        myTextPopup.setSize(bounds.getSize());
        myTextPopup.setLocation(bounds.getLocation());

        myTextPopup.setSize(bounds.getSize());
        myTextPopup.setLocation(bounds.getLocation());

        new MnemonicHelper().register(myTextFieldPanel);
        if (myProject != null && !myProject.isDefault()) {
            DaemonCodeAnalyzer.getInstance(myProject).disableUpdateByTimer(myTextPopup);
        }

        myTextField.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(@NotNull final FocusEvent e) {
                try {
                    Component oppositeComponent = e.getOppositeComponent();

                    if (oppositeComponent != null && !(oppositeComponent instanceof JFrame) &&
                            myList.isShowing() &&
                            (oppositeComponent == myList || SwingUtilities.isDescendingFrom(myList, oppositeComponent))) {
                        myTextField.requestFocus();// Otherwise me may skip some KeyEvents
                    } else {
                        close(false);
                    }
                } catch (Throwable t) {
                    ExceptionTracker.getInstance().trackAndRethrow(t);
                }
            }
        });

        addKeyListeners();

        myTextPopup.show(layeredPane);
    }

    private void addKeyListeners() {
        final Set<KeyStroke> upShortcuts = getShortcuts(IdeActions.ACTION_EDITOR_MOVE_CARET_UP);
        final Set<KeyStroke> downShortcuts = getShortcuts(IdeActions.ACTION_EDITOR_MOVE_CARET_DOWN);

        myTextField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(@NotNull KeyEvent e) {
                try {
                    onKeyPressed(e);
                } catch (Throwable t) {
                    ExceptionTracker.getInstance().trackAndRethrow(t);
                }
            }

            private void onKeyPressed(@NotNull KeyEvent e) {
                if (!myListScrollPane.isVisible()) {
                    return;
                }

                final int keyCode;

                // Add support for user-defined 'caret up/down' shortcuts.
                KeyStroke stroke = KeyStroke.getKeyStrokeForEvent(e);
                if (upShortcuts.contains(stroke)) {
                    keyCode = KeyEvent.VK_UP;
                }
                else if (downShortcuts.contains(stroke)) {
                    keyCode = KeyEvent.VK_DOWN;
                }
                else {
                    keyCode = e.getKeyCode();
                }
                switch (keyCode) {
                    case KeyEvent.VK_DOWN:
                        if (isElementSelected()) {
                            ListScrollingUtil.moveDown(myList, e.getModifiersEx());
                        } else {
                            selectFirst();
                        }

                        updateQueryBasedOnSelection();
                        break;
                    case KeyEvent.VK_UP:
                        if (isElementSelected()) {
                            ListScrollingUtil.moveUp(myList, e.getModifiersEx());
                        } else {
                            selectLast();
                        }

                        updateQueryBasedOnSelection();
                        break;
                    case KeyEvent.VK_PAGE_UP:
                        if (isElementSelected()) {
                            ListScrollingUtil.movePageUp(myList);
                        } else {
                            selectFirst();
                        }

                        updateQueryBasedOnSelection();
                        break;
                    case KeyEvent.VK_PAGE_DOWN:
                        if (isElementSelected()) {
                            ListScrollingUtil.movePageDown(myList);
                        } else {
                            selectLast();
                        }

                        updateQueryBasedOnSelection();
                        break;
                    case KeyEvent.VK_TAB:
                        close(true);
                        break;
                    case KeyEvent.VK_ENTER:
                        break;
                }
//
//                if (myList.getSelectedValue() == NON_PREFIX_SEPARATOR) {
//                    if (keyCode == KeyEvent.VK_UP || keyCode == KeyEvent.VK_PAGE_UP) {
//                        ListScrollingUtil.moveUp(myList, e.getModifiersEx());
//                    }
//                    else {
//                        ListScrollingUtil.moveDown(myList, e.getModifiersEx());
//                    }
//                }
            }
        });

        myTextField.addActionListener(new TrackedActionListener() {
            @Override
            public void doActionPerformed(ActionEvent actionEvent) {
                boolean isSuccess = actionProcessor.processAction(getQuery(), myProject);

                if (isSuccess) {
                    close(true);
                }
            }
        });

        myTextField.getDocument().addDocumentListener(new TrackedDocumentAdapter() {
            @Override
            protected void doTextChanged(DocumentEvent e) {
                currentQuery = null;

                if (isSkipQueryChangeEvent) return;

                Query query = getQuery();

                if (query.isEmptyKey() || query.isValid()) {
                    rebuildList(false); //todo add throttling
                } else {
                    markAsInvalid();
                }
            }
        });
    }

    private void markAsInvalid() {
        myTextField.setForeground(JBColor.red);
    }

    private void markAsValid() {
        myTextField.setForeground(JBColor.black);
    }

    private boolean isElementSelected() {
      return myList.getSelectedIndex() >= 0;
    }

    private void selectFirst() {
        myList.setSelectedIndex(0);
    }

    private void selectLast() {
        myList.setSelectedIndex(Math.max(0, myList.getModel().getSize() - 1));
    }

    private void updateQueryBasedOnSelection() {
        isSkipQueryChangeEvent = true;

        HmListItem item = (HmListItem) myList.getSelectedValue();
        if (item != null) setQuery(item.toQuery());

        isSkipQueryChangeEvent = false;
    }

    private void rebuildList(boolean b) {
        Query query = getQuery();

        setMatcher(query.getKey());
        ArrayList<HmListItem> data = getElementsByPattern(query.getKey(), getItemsToSuggest(query));

        myListModel.removeAllElements();

        for (int i = 0; i < Math.min(data.size(), LIST_SIZE_LIMIT); i++) {
            myListModel.addElement(data.get(i));
        }

        //todo improve algorithm
        Query.Type queryType = query.getType();
        if (myListModel.size() == 0 && !query.isEmptyKey() && (queryType == Query.Type.OPEN || queryType == Query.Type.DELETE || queryType == Query.Type.COMMAND)) {
            markAsInvalid();
        } else {
            markAsValid();
        }

        showList();

        //ChooseByName.rebuildList
//        addElementsByPattern(myPattern, elements, myCancelled, everywhere);
    }

    private Collection<HmListItem> getItemsToSuggest(Query query) {
        java.util.List<HmListItem> items = new ArrayList<HmListItem>();

        if (query.isCommandPrefix()) {
            for (Command command : Command.values()) {
                items.add(new HmListItem(query.getPrefix(), command));
            }
        } else {
            java.util.List<Note> notes = notesService.getNotes();

            for (Note note : notes) {
                items.add(new HmListItem(query.getPrefix(), note));
            }
        }

        return items;
    }

    public ArrayList<HmListItem> getElementsByPattern(@NotNull String pattern, @NotNull Collection<HmListItem> elements) {
        ArrayList<HmListItem> answer = Lists.newArrayList();

        if (!StringUtils.isEmpty(pattern)) {
            final MinusculeMatcher matcher = buildPatternMatcher(addSearchAnywherePatternDecorationIfNeeded(pattern), NameUtil.MatchingCaseSensitivity.NONE);

            for (HmListItem o : elements) {
                if (o != null) {
                    MatchResult result = matches(matcher, getObjectName(o));

                    if (result != null) {
                        answer.add(o);
                    }
                }
            }
        }

        return answer;
    }

    private String getObjectName(Object o) {
        if (o instanceof PsiFile) {
            return ((PsiFile) o).getName();
        } else if (o instanceof Note) {
            return ((HmListItem) o).getName();
        }  else {
            return o.toString();
        }
    }

    private synchronized Query getQuery() {
        if (currentQuery == null) {
            currentQuery = new Query(myTextField.getText());
        }

        return currentQuery;
    }

    private synchronized void setQuery(Query query) {
        currentQuery = query;
        myTextField.setText(query.toString());
    }

    @NotNull
    private static String addSearchAnywherePatternDecorationIfNeeded(@NotNull String pattern) {
        if (!pattern.trim().isEmpty()) {
            pattern = "*" + pattern;
        }
        return pattern;
    }

    @NotNull
    private static Set<KeyStroke> getShortcuts(@NotNull String actionId) {
        Set<KeyStroke> result = new HashSet<KeyStroke>();
        Keymap keymap = KeymapManager.getInstance().getActiveKeymap();
        Shortcut[] shortcuts = keymap.getShortcuts(actionId);
        if (shortcuts == null) {
            return result;
        }
        for (Shortcut shortcut : shortcuts) {
            if (shortcut instanceof KeyboardShortcut) {
                KeyboardShortcut keyboardShortcut = (KeyboardShortcut)shortcut;
                result.add(keyboardShortcut.getFirstKeyStroke());
            }
        }
        return result;
    }

    private JLayeredPane getLayeredPane() {
        JLayeredPane layeredPane;
        final Window window = WindowManager.getInstance().suggestParentWindow(myProject);

        Component parent = UIUtil.findUltimateParent(window);

        if (parent instanceof JFrame) {
            layeredPane = ((JFrame) parent).getLayeredPane();
        } else if (parent instanceof JDialog) {
            layeredPane = ((JDialog) parent).getLayeredPane();
        } else {
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
            } else {
                addElement(element);
            }
        }

        @Override
        public void removeRangeFromModel(int start, int end) {
            if (start < size() && size() != 0) {
                removeRange(start, Math.min(end, size() - 1));
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
                    return ((KeyboardShortcut) shortcut).getFirstKeyStroke();
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
            } else if (LangDataKeys.PARENT_POPUP.equals(key)) {
            }
        }

        @Override
        protected void processKeyEvent(@NotNull KeyEvent e) {
            super.processKeyEvent(e);
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

    private void close(boolean ok) {
        cleanupUI(ok);
    }

    private void cleanupUI(boolean ok) {
        if (myTextPopup != null) {
            if (ok) {
                myTextPopup.closeOk(null);
            } else {
                myTextPopup.cancel();
            }
            myTextPopup = null;
        }

        if (myDropdownPopup != null) {
            if (ok) {
                myDropdownPopup.closeOk(null);
            } else {
                myDropdownPopup.cancel();
            }
            myDropdownPopup = null;
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
                    return ((DataProvider) element).getData(dataId);
                }
            } else if (LangDataKeys.PSI_ELEMENT_ARRAY.is(dataId)) {
                final java.util.List<Object> chosenElements = null;
                if (chosenElements != null) {
                    java.util.List<PsiElement> result = new ArrayList<PsiElement>(chosenElements.size());
                    for (Object element : chosenElements) {
                        if (element instanceof PsiElement) {
                            result.add((PsiElement) element);
                        }
                    }
                    return PsiUtilCore.toPsiElementArray(result);
                }
            } else if (PlatformDataKeys.DOMINANT_HINT_AREA_RECTANGLE.is(dataId)) {
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
