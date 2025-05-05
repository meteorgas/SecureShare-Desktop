package ui;

import network.FileReceiver;
import network.PeerDiscovery;
import utils.Config;
import utils.FileUtils;
import utils.TransferHistoryManager;
import utils.UIStyleUtils;

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
    private TransferHistoryPanel historyPanel;
    private JSplitPane splitPane;

    private String saveDirectory;
    private PeerDiscovery peerDiscovery;
    private FileReceiver fileReceiver;
    private TransferHistoryManager historyManager;
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
        setSize(550, 550);
        setLocationRelativeTo(null);

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout(15, 15));
        mainPanel.setBorder(new EmptyBorder(15, 15, 15, 15));

        // Create a vertical panel for controls
        JPanel controlPanel = new JPanel();
        controlPanel.setLayout(new BoxLayout(controlPanel, BoxLayout.Y_AXIS));
        controlPanel.setBorder(new EmptyBorder(0, 0, 15, 0));

        // Create server settings panel
        JPanel serverPanel = new JPanel(new GridLayout(1, 2, 8, 8));
        serverPanel.setBorder(UIStyleUtils.createSectionBorder("Server Settings"));

        JLabel portLabel = new JLabel("Listening Port:");
        UIStyleUtils.styleLabel(portLabel);
        serverPanel.add(portLabel);

        portField = new JTextField(String.valueOf(Config.DEFAULT_PORT));
        UIStyleUtils.styleTextField(portField);
        serverPanel.add(portField);

        // Create directory panel
        JPanel directoryPanel = new JPanel(new BorderLayout(8, 8));
        directoryPanel.setBorder(UIStyleUtils.createSectionBorder("Save Location"));

        JLabel saveLabel = new JLabel("Files will be saved to:");
        UIStyleUtils.styleLabel(saveLabel);
        directoryPanel.add(saveLabel, BorderLayout.NORTH);

        JPanel dirInputPanel = new JPanel(new BorderLayout(8, 8));
        saveDirectoryField = new JTextField(saveDirectory);
        saveDirectoryField.setEditable(false);
        UIStyleUtils.styleTextField(saveDirectoryField);
        dirInputPanel.add(saveDirectoryField, BorderLayout.CENTER);

        chooseDirButton = new JButton("Browse...");
        UIStyleUtils.styleButton(chooseDirButton);
        dirInputPanel.add(chooseDirButton, BorderLayout.EAST);

        directoryPanel.add(dirInputPanel, BorderLayout.CENTER);

        // Create control buttons panel
        JPanel buttonPanel = new JPanel(new GridLayout(1, 2, 15, 0));
        buttonPanel.setBorder(UIStyleUtils.createSectionBorder("Server Control"));

        startButton = new JButton("Start Receiver");
        UIStyleUtils.styleButton(startButton);

        stopButton = new JButton("Stop Receiver");
        UIStyleUtils.styleButton(stopButton);
        stopButton.setEnabled(false);

        buttonPanel.add(startButton);
        buttonPanel.add(stopButton);

        // Create progress panel with title border
        progressPanel = new ProgressPanel();
        progressPanel.setBorder(UIStyleUtils.createSectionBorder("Transfer Progress"));

        // Create history panel
        historyManager = new TransferHistoryManager();
        historyPanel = new TransferHistoryPanel(historyManager);
        historyPanel.setBorder(UIStyleUtils.createSectionBorder("Transfer History"));

        // Create split pane for progress and history
        splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, progressPanel, historyPanel);
        splitPane.setResizeWeight(0.5); // Equal distribution
        splitPane.setOneTouchExpandable(true);
        splitPane.setContinuousLayout(true);

        // Add components to control panel in vertical order
        controlPanel.add(serverPanel);
        controlPanel.add(Box.createRigidArea(new Dimension(0, 15))); // Spacer
        controlPanel.add(directoryPanel);
        controlPanel.add(Box.createRigidArea(new Dimension(0, 15))); // Spacer
        controlPanel.add(buttonPanel);

        // Add components to main panel
        mainPanel.add(controlPanel, BorderLayout.NORTH);
        mainPanel.add(splitPane, BorderLayout.CENTER);

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

        // Create file receiver with history manager
        fileReceiver = new FileReceiver(saveDirectory, progressPanel::log, progressPanel::updateProgress, historyManager);
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
                fileReceiver = new FileReceiver(saveDirectory, progressPanel::log, progressPanel::updateProgress, historyManager);
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

        // Reset progress bar and show waiting message
        progressPanel.resetProgress();
        progressPanel.log("Waiting for files...");

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
