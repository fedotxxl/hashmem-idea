/*
 * HmSettingsDialog
 * Copyright (c) 2012 Cybervision. All rights reserved.
 */
package com.hashmem.idea.ui;

import com.hashmem.idea.HashMemSettings;
import com.intellij.ui.DocumentAdapter;
import com.intellij.ui.JBColor;
import org.apache.commons.lang.StringUtils;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

public class HmSettingsDialog {

    private HmSettingsForm form;
    private JFrame f;

    public static class HmSettingsForm extends JPanel {

        private JCheckBox sync;
        private JTextField username;
        private JTextField password;
        private JButton checkCredentials;
        private JButton changeUsername;
        private HashMemSettings.Action action = null;
        private boolean hasUsernameOnStartup;

        public HmSettingsForm (HashMemSettings.Model model) {
            super(new BorderLayout());

            final HmSettingsForm _this = this;

            hasUsernameOnStartup = !StringUtils.isEmpty(model.getUsername());

            JPanel labelPanel = new JPanel(new GridLayout(3, 1));
            JPanel fieldPanel = new JPanel(new GridLayout(3, 1));
            add(labelPanel, BorderLayout.WEST);
            add(fieldPanel, BorderLayout.CENTER);

            sync = new JCheckBox();
            sync.setSelected(model.isSyncEnabled());
            sync.addItemListener(new ItemListener() {
                @Override
                public void itemStateChanged(ItemEvent e) {
                    updateUsernameEnabled();
                    updatePasswordEnabled();
                    updateCheckCredentialsEnabled();
                    updateChangeUsernameEnabled();
                }
            });


            username = new JTextField(model.getUsername(), 16);
            username.getDocument().addDocumentListener(new DocumentAdapter() {
                @Override
                protected void textChanged(DocumentEvent e) {
                    updateCheckCredentialsEnabled();
                }
            });

            password = new JPasswordField(model.getPassword(), 16);
            password.setFont(username.getFont());
            password.getDocument().addDocumentListener(new DocumentAdapter() {
                @Override
                protected void textChanged(DocumentEvent e) {
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
            checkCredentials.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    new HmLog().unknownCommand("asd");
                }
            });

            buttons.add(checkCredentials);

            changeUsername = new JButton("Change username");
            changeUsername.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
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
            buttonsAndMessage.add(getLabel("synced successfully", false), BorderLayout.PAGE_END);

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

        private JLabel getLabel(String text, boolean warn) {
            JLabel label = new JLabel(text);
            Font font = label.getFont();
            label.setFont(new Font(font.getFontName(), Font.BOLD, font.getSize()));
            label.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
            label.setForeground((warn) ? JBColor.RED : new Color(98, 150, 85));

            return label;
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

    public static void main(String[] args) {
        //Create and set up the window.
        JFrame frame = new JFrame();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        //Set up the content pane.
        frame.setContentPane(new HmSettingsForm(new HashMemSettings.Model(true, "qwe", "a")));

        //Display the window.
        frame.pack();
        frame.setVisible(true);
    }

    public HmSettingsDialog(HashMemSettings.Model model) {
        f = new JFrame();
        form = new HmSettingsForm(model);
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
