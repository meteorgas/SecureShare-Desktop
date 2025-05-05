package ui;

import network.FileReceiver;
import network.PeerDiscovery;
import utils.Config;
import utils.FileUtils;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/**
 * Window for receiving files over a network connection.
 */
public class ReceiverWindow extends JFrame {
    private JTextField portField;
    private JTextField saveDirectoryField;
    private JButton chooseDirButton;
    private JButton startButton;
    private JButton stopButton;
    private ProgressPanel progressPanel;
    
    private String saveDirectory;
    private PeerDiscovery peerDiscovery;
    private FileReceiver fileReceiver;
    private boolean isRunning = false;
    
    /**
     * Constructor that creates and initializes the GUI.
     */
    public ReceiverWindow() {
        // Use default directory if none provided
        this.saveDirectory = Config.DEFAULT_SAVE_DIRECTORY;
        
        initializeUI();
        initializeNetworking();
    }
    
    /**
     * Sets up the GUI components.
     */
    private void initializeUI() {
        setTitle(Config.RECEIVER_TITLE);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(500, 400);
        setLocationRelativeTo(null);

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout(10, 10));
        mainPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        // Create input panel for port and save directory
        JPanel inputPanel = new JPanel(new GridLayout(2, 3, 5, 5));

        inputPanel.add(new JLabel("Listening Port:"));
        portField = new JTextField(String.valueOf(Config.DEFAULT_PORT));
        inputPanel.add(portField);
        // Empty cell for alignment
        inputPanel.add(new JLabel());

        inputPanel.add(new JLabel("Save Directory:"));
        saveDirectoryField = new JTextField(saveDirectory);
        saveDirectoryField.setEditable(false);
        inputPanel.add(saveDirectoryField);

        chooseDirButton = new JButton("Browse...");
        inputPanel.add(chooseDirButton);

        // Create button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        startButton = new JButton("Start Receiver");
        stopButton = new JButton("Stop Receiver");
        stopButton.setEnabled(false);

        buttonPanel.add(startButton);
        buttonPanel.add(stopButton);

        // Create progress panel
        progressPanel = new ProgressPanel();

        // Add components to main panel
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.add(inputPanel, BorderLayout.NORTH);
        topPanel.add(buttonPanel, BorderLayout.SOUTH);

        mainPanel.add(topPanel, BorderLayout.NORTH);
        mainPanel.add(progressPanel, BorderLayout.CENTER);

        // Add main panel to frame
        add(mainPanel);

        // Add action listeners
        chooseDirButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                chooseDirectory();
            }
        });

        startButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                startReceiving();
            }
        });

        stopButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                stopReceiving();
            }
        });
        
        // Add window listener to clean up resources when window is closed
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                cleanup();
            }
        });

        // Initial log message
        progressPanel.log("Ready to receive files. Click 'Start Receiver' to begin listening.");
    }
    
    /**
     * Initializes the networking components.
     */
    private void initializeNetworking() {
        // Create peer discovery
        peerDiscovery = new PeerDiscovery();
        peerDiscovery.addLogListener(progressPanel::log);
        
        // Create file receiver
        fileReceiver = new FileReceiver(saveDirectory, progressPanel::log, progressPanel::updateProgress);
    }
    
    /**
     * Opens a directory chooser dialog to select a save location.
     */
    private void chooseDirectory() {
        String selectedDir = FileUtils.selectDirectory((JComponent)getContentPane(), "Choose Save Directory");
        
        if (selectedDir != null) {
            saveDirectory = selectedDir;
            saveDirectoryField.setText(saveDirectory);
            progressPanel.log("Save directory set to: " + saveDirectory);
            
            // Update the file receiver with the new directory
            if (fileReceiver != null) {
                // We need to create a new FileReceiver with the updated directory
                fileReceiver = new FileReceiver(saveDirectory, progressPanel::log, progressPanel::updateProgress);
            }
        }
    }
    
    /**
     * Starts the server to listen for incoming file transfers.
     */
    private void startReceiving() {
        if (isRunning) {
            progressPanel.log("Receiver is already running.");
            return;
        }

        int port;
        try {
            port = Integer.parseInt(portField.getText().trim());
        } catch (NumberFormatException e) {
            progressPanel.log("Error: Invalid port number. Using default port " + Config.DEFAULT_PORT);
            port = Config.DEFAULT_PORT;
        }

        // Disable input controls
        portField.setEnabled(false);
        chooseDirButton.setEnabled(false);
        startButton.setEnabled(false);
        stopButton.setEnabled(true);

        // Start the receiver
        if (fileReceiver.start(port)) {
            isRunning = true;
            
            // Start the discovery service
            peerDiscovery.startReceiver(port);
        } else {
            // Re-enable controls if start failed
            portField.setEnabled(true);
            chooseDirButton.setEnabled(true);
            startButton.setEnabled(true);
            stopButton.setEnabled(false);
        }
    }
    
    /**
     * Stops the server and closes all connections.
     */
    private void stopReceiving() {
        if (!isRunning) {
            progressPanel.log("Receiver is not running.");
            return;
        }
        
        // Stop the receiver
        fileReceiver.stop();
        
        // Stop the discovery service
        peerDiscovery.stop();
        
        isRunning = false;
        
        // Re-enable input controls
        portField.setEnabled(true);
        chooseDirButton.setEnabled(true);
        startButton.setEnabled(true);
        stopButton.setEnabled(false);
    }
    
    /**
     * Cleans up resources when the window is closed.
     */
    private void cleanup() {
        if (isRunning) {
            stopReceiving();
        }
    }
}