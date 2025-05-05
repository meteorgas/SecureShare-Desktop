package ui;

import utils.Config;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Main window for the FileBeam application.
 * Provides a graphical interface to choose between sender and receiver modes.
 */
public class MainWindow extends JFrame {

    /**
     * Constructor that creates and initializes the GUI.
     */
    public MainWindow() {
        initializeUI();
    }

    /**
     * Sets up the GUI components.
     */
    private void initializeUI() {
        setTitle(Config.APP_TITLE);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(400, 200);
        setLocationRelativeTo(null);

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout(10, 10));
        mainPanel.setBorder(new EmptyBorder(20, 20, 20, 20));

        // Create title label
        JLabel titleLabel = new JLabel(Config.APP_TITLE);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 16));
        titleLabel.setHorizontalAlignment(JLabel.CENTER);

        // Create button panel
        JPanel buttonPanel = new JPanel(new GridLayout(2, 1, 10, 10));

        JButton senderButton = new JButton("Send a File");
        JButton receiverButton = new JButton("Receive Files");

        buttonPanel.add(senderButton);
        buttonPanel.add(receiverButton);

        // Add components to main panel
        mainPanel.add(titleLabel, BorderLayout.NORTH);
        mainPanel.add(buttonPanel, BorderLayout.CENTER);

        // Add main panel to frame
        add(mainPanel);

        // Add action listeners
        senderButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                launchSender();
            }
        });

        receiverButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                launchReceiver();
            }
        });
    }

    /**
     * Launches the SenderWindow GUI.
     */
    private void launchSender() {
        SwingUtilities.invokeLater(() -> {
            SenderWindow senderWindow = new SenderWindow();
            senderWindow.setVisible(true);
        });
    }

    /**
     * Launches the ReceiverWindow GUI.
     */
    private void launchReceiver() {
        SwingUtilities.invokeLater(() -> {
            ReceiverWindow receiverWindow = new ReceiverWindow();
            receiverWindow.setVisible(true);
        });
    }
}
