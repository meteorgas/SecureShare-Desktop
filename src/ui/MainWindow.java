package ui;

import utils.Config;
import utils.UIStyleUtils;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Main window for the SecureShare application.
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
        setSize(450, 250);
        setLocationRelativeTo(null);

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout(15, 15));
        mainPanel.setBorder(new EmptyBorder(25, 25, 25, 25));

        // Create title label
        JLabel titleLabel = new JLabel(Config.APP_TITLE);
        titleLabel.setFont(UIStyleUtils.TITLE_FONT.deriveFont(20f));
        titleLabel.setForeground(UIStyleUtils.TEXT_PRIMARY); // Use TEXT_PRIMARY as per color palette
        titleLabel.setHorizontalAlignment(JLabel.CENTER);

        // Add a subtle border to the title
        JPanel titlePanel = new JPanel(new BorderLayout());
        titlePanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, UIStyleUtils.SECONDARY_GRAY), // Use SECONDARY_GRAY as per color palette
            BorderFactory.createEmptyBorder(0, 0, 15, 0)
        ));
        titlePanel.add(titleLabel, BorderLayout.CENTER);

        // Create button panel with a titled border
        JPanel buttonPanel = new JPanel(new GridLayout(2, 1, 15, 15));
        buttonPanel.setBorder(UIStyleUtils.createSectionBorder("Choose Mode"));

        JButton senderButton = new JButton("Send a File");
        UIStyleUtils.styleButton(senderButton);

        JButton receiverButton = new JButton("Receive Files");
        UIStyleUtils.styleButton(receiverButton);

        buttonPanel.add(senderButton);
        buttonPanel.add(receiverButton);

        // Add components to main panel
        mainPanel.add(titlePanel, BorderLayout.NORTH);
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
