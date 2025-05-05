package ui;

import network.FileReceiver;
import network.FileSender;
import network.PeerDiscovery;
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
     * Launches the FileSender GUI.
     * This is a temporary implementation until SenderWindow is created.
     */
    private void launchSender() {
        // For now, we'll use the original FileSender class directly
        // This will be replaced with SenderWindow in the future
        try {
            // Create a new instance of the original FileSender class
            Class<?> fileSenderClass = Class.forName("FileSender");
            Object fileSender = fileSenderClass.newInstance();

            // Call the start method
            fileSenderClass.getMethod("start").invoke(fileSender);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                "Error launching sender: " + e.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    /**
     * Launches the FileReceiver GUI.
     * This is a temporary implementation until ReceiverWindow is created.
     */
    private void launchReceiver() {
        // For now, we'll use the original FileReceiver class directly
        // This will be replaced with ReceiverWindow in the future
        try {
            // Use default directory if none provided
            String saveDirectory = Config.DEFAULT_SAVE_DIRECTORY;

            // Create a new instance of the original FileReceiver class
            Class<?> fileReceiverClass = Class.forName("FileReceiver");
            Object fileReceiver = fileReceiverClass.getConstructor(String.class)
                .newInstance(saveDirectory);

            // Call the start method
            fileReceiverClass.getMethod("start").invoke(fileReceiver);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                "Error launching receiver: " + e.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }
}
