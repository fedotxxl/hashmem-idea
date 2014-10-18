/*
 * HmSettingsDialog
 * Copyright (c) 2012 Cybervision. All rights reserved.
 */
package com.hashmem.idea.ui;

import com.intellij.ui.JBColor;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class HmSettingsDialog {

    public static class HmSettingsForm extends JPanel {

        public HmSettingsForm () {
            super(new BorderLayout());
            JPanel labelPanel = new JPanel(new GridLayout(3, 1));
            JPanel fieldPanel = new JPanel(new GridLayout(3, 1));
            add(labelPanel, BorderLayout.WEST);
            add(fieldPanel, BorderLayout.CENTER);

            JCheckBox sync = new JCheckBox();
            JLabel syncLabel = new JLabel("Sync notes", JLabel.RIGHT);

            JTextField username = new JTextField(16);
            JLabel usernameLabel = new JLabel("username", JLabel.RIGHT);

            JTextField password = new JPasswordField(16);
            password.setFont(username.getFont());
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
            JButton checkCredentials = new JButton("Check credentials");

            checkCredentials.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    new HmLog().unknownCommand("asd");
                }
            });

            buttons.add(checkCredentials);
            buttons.add(new JButton("Change username"));

            JPanel buttonsAndMessage = new JPanel(new BorderLayout());
            buttonsAndMessage.add(buttons, BorderLayout.PAGE_START);
            buttonsAndMessage.add(getLabel("synced successfully", false), BorderLayout.PAGE_END);

            add(buttonsAndMessage, BorderLayout.SOUTH);
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
    }

    public static void main(String[] args) {
        //Create and set up the window.
        JFrame frame = new JFrame("SpringForm");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        //Set up the content pane.
        frame.setContentPane(new HmSettingsForm());

        //Display the window.
        frame.pack();
        frame.setVisible(true);
    }

    public JComponent getRootComponent() {
        JFrame f = new JFrame();
        f.getContentPane().add(new HmSettingsForm(), BorderLayout.NORTH);
        return f.getRootPane();
    }
}
