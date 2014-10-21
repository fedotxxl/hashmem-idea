/*
 * HmSettingsDialog
 * Copyright (c) 2012 Cybervision. All rights reserved.
 */
package com.hashmem.idea.ui;

import com.hashmem.idea.HashMemSettings;
import com.hashmem.idea.remote.AuthService;
import com.hashmem.idea.tracked.TrackedActionListener;
import com.hashmem.idea.tracked.TrackedDocumentAdapter;
import com.hashmem.idea.tracked.TrackedItemListener;
import com.intellij.ui.JBColor;
import org.apache.commons.lang.StringUtils;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.text.SimpleDateFormat;
import java.util.Date;

public class HmSettingsDialog {

    private HmSettingsForm form;
    private JFrame f;

    public static class HmSettingsForm extends JPanel {

        private SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
        private JCheckBox sync;
        private JTextField username;
        private JTextField password;
        private JButton checkCredentials;
        private JButton changeUsername;
        private JLabel notification;
        private HashMemSettings.Action action = null;
        private boolean hasUsernameOnStartup;

        public HmSettingsForm (HashMemSettings.Model model, final AuthService authService) {
            super(new BorderLayout());

            final HmSettingsForm _this = this;

            hasUsernameOnStartup = !StringUtils.isEmpty(model.getUsername());

            JPanel labelPanel = new JPanel(new GridLayout(3, 1));
            JPanel fieldPanel = new JPanel(new GridLayout(3, 1));
            add(labelPanel, BorderLayout.WEST);
            add(fieldPanel, BorderLayout.CENTER);

            sync = new JCheckBox();
            sync.setSelected(model.isSyncEnabled());
            sync.addItemListener(new TrackedItemListener() {
                @Override
                public void doItemStateChanged(ItemEvent e) {
                    updateUsernameEnabled();
                    updatePasswordEnabled();
                    updateCheckCredentialsEnabled();
                    updateChangeUsernameEnabled();
                }
            });


            username = new JTextField(model.getUsername(), 16);
            username.getDocument().addDocumentListener(new TrackedDocumentAdapter() {
                @Override
                protected void doTextChanged(DocumentEvent e) {
                    updateCheckCredentialsEnabled();
                }
            });

            password = new JPasswordField(model.getPassword(), 16);
            password.setFont(username.getFont());
            password.getDocument().addDocumentListener(new TrackedDocumentAdapter() {
                @Override
                protected void doTextChanged(DocumentEvent e) {
                    updateCheckCredentialsEnabled();

                }
            });

            JLabel usernameLabel = new JLabel("username", JLabel.RIGHT);
            JLabel syncLabel = new JLabel("Sync notes", JLabel.RIGHT);
            JLabel passwordLabel = new JLabel("password", JLabel.RIGHT);

            syncLabel.setLabelFor(sync);
            usernameLabel.setLabelFor(username);
            passwordLabel.setLabelFor(password);

            addField(fieldPanel, sync);
            addField(fieldPanel, username);
            addField(fieldPanel, password);
            labelPanel.add(syncLabel);
            labelPanel.add(usernameLabel);
            labelPanel.add(passwordLabel);

            JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT));
            checkCredentials = new JButton("Check credentials");
            checkCredentials.addActionListener(new TrackedActionListener() {
                @Override
                public void doActionPerformed(ActionEvent e) {
                    AuthService.Result result = authService.tryAuthenticate(getUsername(), getPassword());
                    if (result == AuthService.Result.SUCCESS) {
                        displayInfoMessage("Successfully logged in");
                    } else if (result == AuthService.Result.NOT_AUTHENTICATED) {
                        displayWarnMessage("Unable to authenticate. Is username/password correct?");
                    } else if (result == AuthService.Result.IO_EXCEPTION) {
                        displayWarnMessage("Unable to connect to hashMem.com.");
                    } else {
                        displayWarnMessage("Unknown problem");
                    }
                }
            });

            buttons.add(checkCredentials);

            changeUsername = new JButton("Change username");
            changeUsername.addActionListener(new TrackedActionListener() {
                @Override
                public void doActionPerformed(ActionEvent e) {
                    String[] options = { "Unlink", "Unlink and remove local notes", "Cancel" };
                    JPanel panel = new JPanel();
                    panel.add(new JLabel("<html>If you will change username your local notes will be synced with new account.<br>" +
                            "To prevent it click 'Unlink and remove local notes'</html>"), BorderLayout.CENTER);
                    int selected = JOptionPane.showOptionDialog(
                            _this, panel,"Confirmation", JOptionPane.YES_NO_CANCEL_OPTION,
                            JOptionPane.PLAIN_MESSAGE, null, options, options[0]);

                    if (selected == 0) {
                        action = HashMemSettings.Action.UNLINK;
                    } else if (selected == 1) {
                        action = HashMemSettings.Action.UNLINK_AND_RESET;
                    }

                    updateUsernameEnabled();
                    updateChangeUsernameEnabled();
                }
            });

            buttons.add(changeUsername);

            JPanel buttonsAndMessage = new JPanel(new BorderLayout());
            buttonsAndMessage.add(buttons, BorderLayout.PAGE_START);

            notification = new JLabel();
            Font font = notification.getFont();
            notification.setFont(new Font(font.getFontName(), Font.BOLD, font.getSize()));
            notification.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
            buttonsAndMessage.add(notification, BorderLayout.PAGE_END);

            add(buttonsAndMessage, BorderLayout.SOUTH);

            updateUsernameEnabled();
            updatePasswordEnabled();
            updateCheckCredentialsEnabled();
            updateChangeUsernameEnabled();
        }

        private void addField(JPanel fieldPanel, Component component) {
            JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT));
            p.add(component);
            fieldPanel.add(p);
        }

        private void displayInfoMessage(String text) {
            setMessage(text);
            notification.setForeground(new Color(98, 150, 85));
        }

        private void displayWarnMessage(String text) {
            setMessage(text);
            notification.setForeground(JBColor.RED);
        }

        private void setMessage(String message) {
            notification.setText(dateFormat.format(new Date()) + ": " + message);
        }

        private void updateUsernameEnabled() {
            boolean isEnabled = true;

            if (hasUsernameOnStartup && action == null) {
                isEnabled = false;
            } else if (!isSyncEnabled()) {
                isEnabled = false;
            }

            username.setEnabled(isEnabled);
        }

        private void updatePasswordEnabled() {
            password.setEnabled(isSyncEnabled());
        }

        private void updateCheckCredentialsEnabled() {
            boolean isEnabled = true;

            if (!isSyncEnabled()) {
                isEnabled = false;
            } else if (StringUtils.isEmpty(getUsername()) || StringUtils.isEmpty(getPassword())) {
                isEnabled = false;
            }

            checkCredentials.setEnabled(isEnabled);
        }

        private void updateChangeUsernameEnabled() {
            boolean isEnabled = true;

            if (!hasUsernameOnStartup) {
                isEnabled = false;
            } else if (action != null) {
                isEnabled = false;
            }

            changeUsername.setEnabled(isEnabled);
        }

        private boolean isSyncEnabled() {
            return sync.isSelected();
        }

        private String getUsername() {
            return username.getText();
        }

        private String getPassword() {
            return password.getText();
        }

        public HashMemSettings.Model getModel() {
            return new HashMemSettings.Model(isSyncEnabled(), getUsername(), getPassword());
        }

        public HashMemSettings.Action getAppliedAction() {
            return action;
        }
    }

    public HmSettingsDialog(HashMemSettings.Model model, AuthService authService) {
        f = new JFrame();
        form = new HmSettingsForm(model, authService);
        f.getContentPane().add(form, BorderLayout.NORTH);
    }

    public JComponent getRootComponent() {
        return f.getRootPane();
    }

    public HashMemSettings.Model getModel() {
        return form.getModel();
    }

    public HashMemSettings.Action getAppliedAction() {
        return form.getAppliedAction();
    }
}
