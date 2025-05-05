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
    private JLabel fileLabel;
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

        JPanel fileSelectionPanel = new JPanel(new BorderLayout(8, 8));
        fileLabel = new JLabel("No file selected");
        fileLabel.setForeground(Color.GRAY);
        UIStyleUtils.styleLabel(fileLabel);
        fileSelectionPanel.add(fileLabel, BorderLayout.CENTER);

        selectFileButton = new JButton("Select File");
        UIStyleUtils.styleButton(selectFileButton);
        fileSelectionPanel.add(selectFileButton, BorderLayout.EAST);

        // Add drag-drop instruction
        JLabel dragDropLabel = new JLabel("or drag and drop a file here");
        dragDropLabel.setHorizontalAlignment(JLabel.CENTER);
        dragDropLabel.setForeground(new Color(127, 140, 141));
        UIStyleUtils.styleLabel(dragDropLabel);

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
                        fileLabel.setText(selectedFile.getName());
                        fileLabel.setForeground(Color.BLACK);

                        progressPanel.log("File dropped: " + selectedFile.getAbsolutePath());
                        sendFileButton.setEnabled(true);
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
            // Update the file label
            fileLabel.setText(selectedFile.getName());
            fileLabel.setForeground(Color.BLACK);

            progressPanel.log("Selected file: " + selectedFile.getAbsolutePath());
            sendFileButton.setEnabled(true);
        } else {
            // Reset the file label
            fileLabel.setText("No file selected");
            fileLabel.setForeground(Color.GRAY);

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
