package ui;

import network.FileSender;
import network.PeerDiscovery;
import utils.Config;
import utils.FileUtils;
import utils.TransferHistoryManager;
import utils.UIStyleUtils;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;

/**
 * Window for selecting and sending files over a network connection.
 */
public class SenderWindow extends JFrame {
    private JTextField ipAddressField;
    private JTextField portField;
    private JButton selectFileButton;
    private JButton clearFileButton;
    private JButton sendFileButton;
    private JButton searchDevicesButton;
    private JLabel fileLabel;
    private JLabel fileSizeLabel;
    private ProgressPanel progressPanel;
    private TransferHistoryPanel historyPanel;
    private JSplitPane splitPane;

    private File selectedFile;
    private PeerDiscovery peerDiscovery;
    private FileSender fileSender;
    private TransferHistoryManager historyManager;

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
        setSize(550, 550);
        setLocationRelativeTo(null);

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout(15, 15));
        mainPanel.setBorder(new EmptyBorder(15, 15, 15, 15));

        // Create a vertical panel for controls
        JPanel controlPanel = new JPanel();
        controlPanel.setLayout(new BoxLayout(controlPanel, BoxLayout.Y_AXIS));
        controlPanel.setBorder(new EmptyBorder(0, 0, 15, 0));

        // Create connection panel
        JPanel connectionPanel = new JPanel(new GridLayout(2, 2, 8, 8));
        connectionPanel.setBorder(UIStyleUtils.createSectionBorder("Connection Settings"));

        JLabel ipLabel = new JLabel("Receiver IP Address:");
        UIStyleUtils.styleLabel(ipLabel);
        connectionPanel.add(ipLabel);

        ipAddressField = new JTextField(Config.DEFAULT_IP);
        UIStyleUtils.styleTextField(ipAddressField);
        connectionPanel.add(ipAddressField);

        JLabel portLabel = new JLabel("Port:");
        UIStyleUtils.styleLabel(portLabel);
        connectionPanel.add(portLabel);

        portField = new JTextField(String.valueOf(Config.DEFAULT_PORT));
        UIStyleUtils.styleTextField(portField);
        connectionPanel.add(portField);

        // Create file selection panel
        JPanel filePanel = new JPanel(new BorderLayout(8, 8));
        filePanel.setBorder(UIStyleUtils.createSectionBorder("File Selection"));

        // Create a panel for file information (name and size)
        JPanel fileInfoPanel = new JPanel(new BorderLayout(4, 4));

        // File name label
        fileLabel = new JLabel("No file selected");
        fileLabel.setForeground(UIStyleUtils.TEXT_SECONDARY);
        UIStyleUtils.styleLabel(fileLabel);
        fileInfoPanel.add(fileLabel, BorderLayout.NORTH);

        // File size label
        fileSizeLabel = new JLabel("");
        fileSizeLabel.setForeground(UIStyleUtils.TEXT_SECONDARY);
        UIStyleUtils.styleHelperLabel(fileSizeLabel);
        fileInfoPanel.add(fileSizeLabel, BorderLayout.CENTER);

        // Panel for file selection buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));

        // Clear file button
        clearFileButton = new JButton("Clear Selection");
        UIStyleUtils.styleButton(clearFileButton);
        clearFileButton.setEnabled(false); // Disable until file is selected
        buttonPanel.add(clearFileButton);

        // Select file button
        selectFileButton = new JButton("Select File");
        UIStyleUtils.styleButton(selectFileButton);
        buttonPanel.add(selectFileButton);

        // Add components to file selection panel
        JPanel fileSelectionPanel = new JPanel(new BorderLayout(8, 8));
        fileSelectionPanel.add(fileInfoPanel, BorderLayout.CENTER);
        fileSelectionPanel.add(buttonPanel, BorderLayout.EAST);

        // Add drag-drop instruction with helper text styling as per Task 8
        JLabel dragDropLabel = new JLabel("or drag and drop a file here");
        UIStyleUtils.styleHelperLabel(dragDropLabel);

        filePanel.add(fileSelectionPanel, BorderLayout.CENTER);
        filePanel.add(dragDropLabel, BorderLayout.SOUTH);

        // Create action panel
        JPanel actionPanel = new JPanel(new GridLayout(1, 2, 15, 0));
        actionPanel.setBorder(UIStyleUtils.createSectionBorder("Actions"));

        searchDevicesButton = new JButton("Search Devices");
        UIStyleUtils.styleButton(searchDevicesButton);

        sendFileButton = new JButton("Send File");
        UIStyleUtils.styleButton(sendFileButton);
        sendFileButton.setEnabled(false); // Disable until file is selected

        actionPanel.add(searchDevicesButton);
        actionPanel.add(sendFileButton);

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
        controlPanel.add(connectionPanel);
        controlPanel.add(Box.createRigidArea(new Dimension(0, 15))); // Spacer
        controlPanel.add(filePanel);
        controlPanel.add(Box.createRigidArea(new Dimension(0, 15))); // Spacer
        controlPanel.add(actionPanel);

        // Add components to main panel
        mainPanel.add(controlPanel, BorderLayout.NORTH);
        mainPanel.add(splitPane, BorderLayout.CENTER);

        // Add main panel to frame
        add(mainPanel);

        // Set up drag and drop
        setupDragAndDrop(filePanel);

        // Add action listeners
        selectFileButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                selectFile();
            }
        });

        clearFileButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                clearFileSelection();
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
     * Sets up drag and drop functionality for the specified component.
     * 
     * @param component The component to enable drag and drop on
     */
    private void setupDragAndDrop(JComponent component) {
        new DropTarget(component, new DropTargetAdapter() {
            @Override
            public void dragEnter(DropTargetDragEvent dtde) {
                // Check if the dragged data is a file
                if (isDraggedDataFile(dtde)) {
                    dtde.acceptDrag(DnDConstants.ACTION_COPY);
                    component.setBorder(BorderFactory.createCompoundBorder(
                        UIStyleUtils.createSectionBorder("File Selection"),
                        BorderFactory.createLineBorder(new Color(52, 152, 219), 2)
                    ));
                } else {
                    dtde.rejectDrag();
                }
            }

            @Override
            public void dragExit(DropTargetEvent dte) {
                // Reset the border when drag exits
                component.setBorder(UIStyleUtils.createSectionBorder("File Selection"));
            }

            @Override
            public void drop(DropTargetDropEvent dtde) {
                try {
                    // Reset the border
                    component.setBorder(UIStyleUtils.createSectionBorder("File Selection"));

                    // Accept the drop
                    dtde.acceptDrop(DnDConstants.ACTION_COPY);

                    // Get the dropped files
                    Transferable transferable = dtde.getTransferable();

                    @SuppressWarnings("unchecked")
                    List<File> files = (List<File>) transferable.getTransferData(DataFlavor.javaFileListFlavor);

                    if (!files.isEmpty()) {
                        // Use the first file
                        File droppedFile = files.get(0);

                        // Set as selected file
                        selectedFile = droppedFile;

                        // Update file info and enable buttons
                        updateFileInfo(selectedFile);
                        sendFileButton.setEnabled(true);
                        clearFileButton.setEnabled(true);

                        progressPanel.log("File dropped: " + selectedFile.getAbsolutePath());
                    }

                    dtde.dropComplete(true);
                } catch (Exception e) {
                    progressPanel.log("Error handling dropped file: " + e.getMessage());
                    dtde.dropComplete(false);
                }
            }
        });
    }

    /**
     * Checks if the dragged data is a file.
     * 
     * @param dtde The drag event
     * @return true if the dragged data is a file, false otherwise
     */
    private boolean isDraggedDataFile(DropTargetDragEvent dtde) {
        return dtde.isDataFlavorSupported(DataFlavor.javaFileListFlavor);
    }

    /**
     * Initializes the networking components.
     */
    private void initializeNetworking() {
        // Create peer discovery
        peerDiscovery = new PeerDiscovery();
        peerDiscovery.addLogListener(progressPanel::log);

        // Create file sender with history manager
        fileSender = new FileSender(progressPanel::log, progressPanel::updateProgress, historyManager);
    }

    /**
     * Opens a file chooser dialog to select a file.
     */
    private void selectFile() {
        selectedFile = FileUtils.selectFile((JComponent)getContentPane(), "Select a file to send");

        if (selectedFile != null) {
            // Update the file label and size
            updateFileInfo(selectedFile);

            // Enable buttons
            sendFileButton.setEnabled(true);
            clearFileButton.setEnabled(true);

            progressPanel.log("Selected file: " + selectedFile.getAbsolutePath());
        } else {
            // Reset the file selection
            clearFileSelection();

            progressPanel.log("File selection cancelled.");
        }
    }

    /**
     * Clears the current file selection.
     */
    private void clearFileSelection() {
        // Reset the file variable
        selectedFile = null;

        // Reset the file labels
        fileLabel.setText("No file selected");
        fileLabel.setForeground(UIStyleUtils.TEXT_SECONDARY);
        fileSizeLabel.setText("");

        // Disable buttons
        sendFileButton.setEnabled(false);
        clearFileButton.setEnabled(false);

        progressPanel.log("File selection cleared.");
    }

    /**
     * Updates the file information display with name, size, and preview.
     * 
     * @param file The file to display information for
     */
    private void updateFileInfo(File file) {
        if (file == null) return;

        // Update file name
        fileLabel.setText(file.getName());
        fileLabel.setForeground(Color.BLACK);

        // Update file size in MB
        double fileSizeMB = file.length() / (1024.0 * 1024.0);
        fileSizeLabel.setText(String.format("Size: %.2f MB", fileSizeMB));

        // Create preview for image or text files
        createFilePreview(file);
    }

    /**
     * Creates a preview for image or text files.
     * 
     * @param file The file to create a preview for
     */
    private void createFilePreview(File file) {
        if (file == null) return;

        String extension = FileUtils.getFileExtension(file.getName()).toLowerCase();

        // Handle image files (JPG, PNG)
        if (extension.equals("jpg") || extension.equals("jpeg") || extension.equals("png")) {
            try {
                // Create a scaled image icon for the preview
                ImageIcon originalIcon = new ImageIcon(file.getPath());
                int maxSize = 200; // Maximum preview size

                // Scale the image if it's larger than maxSize
                if (originalIcon.getIconWidth() > maxSize || originalIcon.getIconHeight() > maxSize) {
                    double scale = Math.min(
                        (double)maxSize / originalIcon.getIconWidth(),
                        (double)maxSize / originalIcon.getIconHeight()
                    );

                    int scaledWidth = (int)(originalIcon.getIconWidth() * scale);
                    int scaledHeight = (int)(originalIcon.getIconHeight() * scale);

                    ImageIcon scaledIcon = new ImageIcon(
                        originalIcon.getImage().getScaledInstance(scaledWidth, scaledHeight, Image.SCALE_SMOOTH)
                    );

                    // Create a tooltip with the image preview
                    fileLabel.setToolTipText("<html><img src='" + file.toURI() + "'></html>");

                    progressPanel.log("Image preview created.");
                }
            } catch (Exception e) {
                progressPanel.log("Error creating image preview: " + e.getMessage());
            }
        }
        // Handle text files (TXT)
        else if (extension.equals("txt")) {
            try {
                // Read the first 100 characters of the text file
                StringBuilder preview = new StringBuilder();
                try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                    char[] buffer = new char[100];
                    int charsRead = reader.read(buffer, 0, 100);

                    if (charsRead > 0) {
                        preview.append(buffer, 0, charsRead);
                        if (charsRead == 100) {
                            preview.append("...");
                        }
                    }
                }

                // Create a tooltip with the text preview
                fileLabel.setToolTipText("<html><pre>" + preview.toString() + "</pre></html>");

                progressPanel.log("Text preview created.");
            } catch (Exception e) {
                progressPanel.log("Error creating text preview: " + e.getMessage());
            }
        } else {
            // Clear any existing tooltip for other file types
            fileLabel.setToolTipText(null);
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

        // Reset progress bar and show sending message
        progressPanel.resetProgress();
        progressPanel.log("Sending file: " + selectedFile.getName() + "...");

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
