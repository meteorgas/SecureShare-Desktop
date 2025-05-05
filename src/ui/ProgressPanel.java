package ui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * A reusable panel for displaying progress information during file transfers.
 */
public class ProgressPanel extends JPanel {
    private JTextArea logArea;
    private JScrollPane scrollPane;
    private JProgressBar progressBar;

    /**
     * Creates a new ProgressPanel with a text area for logs and a progress bar.
     */
    public ProgressPanel() {
        setLayout(new BorderLayout(5, 5));
        setBorder(new EmptyBorder(5, 5, 5, 5));
        
        // Create log area
        logArea = new JTextArea();
        logArea.setEditable(false);
        logArea.setLineWrap(true);
        logArea.setWrapStyleWord(true);
        scrollPane = new JScrollPane(logArea);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        
        // Create progress bar (initially invisible)
        progressBar = new JProgressBar(0, 100);
        progressBar.setStringPainted(true);
        progressBar.setVisible(false);
        
        // Add components to panel
        add(scrollPane, BorderLayout.CENTER);
        add(progressBar, BorderLayout.SOUTH);
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
}