package ui;

import utils.UIStyleUtils;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * A reusable panel for displaying progress information during file transfers.
 */
public class ProgressPanel extends JPanel {
    private JTextArea logArea;
    private JScrollPane scrollPane;
    private JProgressBar progressBar;
    private JButton clearLogButton;

    /**
     * Creates a new ProgressPanel with a text area for logs and a progress bar.
     */
    public ProgressPanel() {
        setLayout(new BorderLayout(8, 8));
        setBorder(new EmptyBorder(8, 8, 8, 8));

        // Create log area with styling
        logArea = new JTextArea();
        logArea.setEditable(false);
        logArea.setLineWrap(true);
        logArea.setWrapStyleWord(true);
        UIStyleUtils.styleTextArea(logArea);

        // Create scroll pane with styled border using SECONDARY_GRAY
        scrollPane = new JScrollPane(logArea);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        scrollPane.setBorder(BorderFactory.createLineBorder(UIStyleUtils.SECONDARY_GRAY, 1));

        // Create progress bar with styling (initially invisible)
        progressBar = new JProgressBar(0, 100);
        progressBar.setStringPainted(true);
        progressBar.setVisible(false);
        progressBar.setFont(UIStyleUtils.REGULAR_FONT);
        progressBar.setForeground(UIStyleUtils.PRIMARY_BLUE); // Use PRIMARY_BLUE as per color palette

        // Create clear log button with styling
        clearLogButton = new JButton("Clear Log");
        UIStyleUtils.styleButton(clearLogButton);
        clearLogButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                clearLog();
            }
        });

        // Create a panel for the button
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.add(clearLogButton);

        // Create a panel for the progress bar and button with spacing
        JPanel southPanel = new JPanel(new BorderLayout(8, 0));
        southPanel.add(progressBar, BorderLayout.CENTER);
        southPanel.add(buttonPanel, BorderLayout.EAST);
        southPanel.setBorder(BorderFactory.createEmptyBorder(8, 0, 0, 0));

        // Add components to panel
        add(scrollPane, BorderLayout.CENTER);
        add(southPanel, BorderLayout.SOUTH);
    }

    /**
     * Adds a message to the log area.
     * 
     * @param message The message to add
     */
    public void log(String message) {
        SwingUtilities.invokeLater(() -> {
            logArea.append(message + "\n");
            // Auto-scroll to bottom
            logArea.setCaretPosition(logArea.getDocument().getLength());
        });
    }

    /**
     * Updates the progress bar with a new percentage value.
     * 
     * @param percentage The progress percentage (0-100)
     */
    public void updateProgress(int percentage) {
        SwingUtilities.invokeLater(() -> {
            if (!progressBar.isVisible()) {
                progressBar.setVisible(true);
            }
            progressBar.setValue(percentage);
        });
    }

    /**
     * Resets the progress bar to 0% and hides it.
     */
    public void resetProgress() {
        SwingUtilities.invokeLater(() -> {
            progressBar.setValue(0);
            progressBar.setVisible(false);
        });
    }

    /**
     * Gets the log text area component.
     * 
     * @return The JTextArea used for logging
     */
    public JTextArea getLogArea() {
        return logArea;
    }

    /**
     * Gets the progress bar component.
     * 
     * @return The JProgressBar used for showing progress
     */
    public JProgressBar getProgressBar() {
        return progressBar;
    }

    /**
     * Clears the log text area.
     */
    public void clearLog() {
        SwingUtilities.invokeLater(() -> {
            logArea.setText("");
        });
    }
}
