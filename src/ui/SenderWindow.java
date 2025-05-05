package ui;

import network.FileSender;
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
import java.io.File;
import java.util.List;

/**
 * Window for selecting and sending files over a network connection.
 */
public class SenderWindow extends JFrame {
    private JTextField ipAddressField;
    private JTextField portField;
    private JButton selectFileButton;
    private JButton sendFileButton;
    private JButton searchDevicesButton;
    private ProgressPanel progressPanel;

    private File selectedFile;
    private PeerDiscovery peerDiscovery;
    private FileSender fileSender;

    /**
     * Constructor that creates and initializes the GUI.
     */
    public SenderWindow() {
        initializeUI();
        initializeNetworking();
    }

    /**
     * Sets up the GUI components.
     */
    private void initializeUI() {
        setTitle(Config.SENDER_TITLE);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(500, 400);
        setLocationRelativeTo(null);

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout(10, 10));
        mainPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        // Create input panel for IP and port
        JPanel inputPanel = new JPanel(new GridLayout(2, 2, 5, 5));
        inputPanel.add(new JLabel("Receiver IP Address:"));
        ipAddressField = new JTextField(Config.DEFAULT_IP);
        inputPanel.add(ipAddressField);

        inputPanel.add(new JLabel("Port:"));
        portField = new JTextField(String.valueOf(Config.DEFAULT_PORT));
        inputPanel.add(portField);

        // Create button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        selectFileButton = new JButton("Select File");
        sendFileButton = new JButton("Send File");
        searchDevicesButton = new JButton("Search Devices");
        sendFileButton.setEnabled(false); // Disable until file is selected

        buttonPanel.add(selectFileButton);
        buttonPanel.add(searchDevicesButton);
        buttonPanel.add(sendFileButton);

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
        selectFileButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                selectFile();
            }
        });

        sendFileButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                sendFile();
            }
        });

        searchDevicesButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                searchDevices();
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
        progressPanel.log("Ready to send files. Please select a file and enter receiver details.");
    }

    /**
     * Initializes the networking components.
     */
    private void initializeNetworking() {
        // Create peer discovery
        peerDiscovery = new PeerDiscovery();
        peerDiscovery.addLogListener(progressPanel::log);

        // Create file sender
        fileSender = new FileSender(progressPanel::log, progressPanel::updateProgress);
    }

    /**
     * Opens a file chooser dialog to select a file.
     */
    private void selectFile() {
        selectedFile = FileUtils.selectFile((JComponent)getContentPane(), "Select a file to send");

        if (selectedFile != null) {
            progressPanel.log("Selected file: " + selectedFile.getAbsolutePath());
            sendFileButton.setEnabled(true);
        } else {
            progressPanel.log("File selection cancelled.");
        }
    }

    /**
     * Initiates the file sending process.
     */
    private void sendFile() {
        if (selectedFile == null) {
            progressPanel.log("Error: No file selected.");
            return;
        }

        // Get IP address and port
        String ipAddress = ipAddressField.getText().trim();
        int port;

        try {
            port = Integer.parseInt(portField.getText().trim());
        } catch (NumberFormatException e) {
            progressPanel.log("Error: Invalid port number. Using default port " + Config.DEFAULT_PORT);
            port = Config.DEFAULT_PORT;
        }

        // Disable buttons during transfer
        selectFileButton.setEnabled(false);
        sendFileButton.setEnabled(false);
        searchDevicesButton.setEnabled(false);

        // Send the file
        fileSender.sendFile(selectedFile, ipAddress, port);

        // Re-enable buttons after a short delay to allow the worker to start
        Timer timer = new Timer(500, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                selectFileButton.setEnabled(true);
                sendFileButton.setEnabled(selectedFile != null);
                searchDevicesButton.setEnabled(true);
            }
        });
        timer.setRepeats(false);
        timer.start();
    }

    /**
     * Searches for available receiver devices on the network.
     */
    private void searchDevices() {
        // Disable the search button during discovery
        searchDevicesButton.setEnabled(false);

        // Search for devices
        peerDiscovery.searchDevices(this::showDeviceSelectionDialog);
    }

    /**
     * Displays a dialog with discovered devices and allows the user to select one.
     * 
     * @param devices The list of discovered devices
     */
    private void showDeviceSelectionDialog(List<PeerDiscovery.ReceiverDevice> devices) {
        // Re-enable the search button
        searchDevicesButton.setEnabled(true);

        if (devices.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "No receiver devices found. Please ensure receivers are running and try again.",
                "No Devices Found",
                JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        PeerDiscovery.ReceiverDevice[] devicesArray = devices.toArray(new PeerDiscovery.ReceiverDevice[0]);
        PeerDiscovery.ReceiverDevice selectedDevice = (PeerDiscovery.ReceiverDevice) JOptionPane.showInputDialog(
            this,
            "Select a receiver device:",
            "Device Selection",
            JOptionPane.QUESTION_MESSAGE,
            null,
            devicesArray,
            devicesArray[0]);

        if (selectedDevice != null) {
            // Update the IP and port fields with the selected device
            ipAddressField.setText(selectedDevice.getIpAddress());
            portField.setText(String.valueOf(selectedDevice.getPort()));
            progressPanel.log("Selected device: " + selectedDevice);
        }
    }

    /**
     * Cleans up resources when the window is closed.
     */
    private void cleanup() {
        // Nothing to clean up for now
    }
}
