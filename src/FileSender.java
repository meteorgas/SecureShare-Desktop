import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.List;

/**
 * FileSender class for selecting and sending files over a network connection.
 * Provides a Swing GUI for user interaction.
 */
public class FileSender extends JFrame {
    private static final int DEFAULT_PORT = 5050;
    private static final String DEFAULT_IP = "127.0.0.1";
    private static final int DISCOVERY_PORT = 8888;
    private static final int DISCOVERY_TIMEOUT = 3000; // 3 seconds

    private JTextField ipAddressField;
    private JTextField portField;
    private JButton selectFileButton;
    private JButton sendFileButton;
    private JButton searchDevicesButton;
    private JTextArea logArea;
    private JScrollPane scrollPane;
    private File selectedFile;

    // For device discovery
    private List<ReceiverDevice> discoveredDevices = new ArrayList<>();

    // Class to represent a discovered receiver device
    private static class ReceiverDevice {
        private final String name;
        private final String ipAddress;
        private final int port;

        public ReceiverDevice(String name, String ipAddress, int port) {
            this.name = name;
            this.ipAddress = ipAddress;
            this.port = port;
        }

        @Override
        public String toString() {
            return name + " (" + ipAddress + ")";
        }
    }

    /**
     * Constructor that creates and initializes the GUI.
     */
    public FileSender() {
        initializeUI();
    }

    /**
     * Sets up the GUI components.
     */
    private void initializeUI() {
        setTitle("FileBeam - File Sender");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(500, 400);
        setLocationRelativeTo(null);

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout(10, 10));
        mainPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        // Create input panel for IP and port
        JPanel inputPanel = new JPanel(new GridLayout(2, 2, 5, 5));
        inputPanel.add(new JLabel("Receiver IP Address:"));
        ipAddressField = new JTextField(DEFAULT_IP);
        inputPanel.add(ipAddressField);

        inputPanel.add(new JLabel("Port:"));
        portField = new JTextField(String.valueOf(DEFAULT_PORT));
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

        // Create log area
        logArea = new JTextArea();
        logArea.setEditable(false);
        logArea.setLineWrap(true);
        logArea.setWrapStyleWord(true);
        scrollPane = new JScrollPane(logArea);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

        // Add components to main panel
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.add(inputPanel, BorderLayout.NORTH);
        topPanel.add(buttonPanel, BorderLayout.SOUTH);

        mainPanel.add(topPanel, BorderLayout.NORTH);
        mainPanel.add(scrollPane, BorderLayout.CENTER);

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

        // Initial log message
        log("Ready to send files. Please select a file and enter receiver details.");
    }

    /**
     * Opens a file chooser dialog to select a file.
     */
    private void selectFile() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Select a file to send");

        int result = fileChooser.showOpenDialog(this);

        if (result == JFileChooser.APPROVE_OPTION) {
            selectedFile = fileChooser.getSelectedFile();
            log("Selected file: " + selectedFile.getAbsolutePath());
            sendFileButton.setEnabled(true);
        } else {
            log("File selection cancelled.");
        }
    }

    /**
     * Initiates the file sending process in a background thread.
     */
    private void sendFile() {
        if (selectedFile == null) {
            log("Error: No file selected.");
            return;
        }

        // Get IP address and port
        String ipAddress = ipAddressField.getText().trim();
        int port;

        try {
            port = Integer.parseInt(portField.getText().trim());
        } catch (NumberFormatException e) {
            log("Error: Invalid port number. Using default port " + DEFAULT_PORT);
            port = DEFAULT_PORT;
        }

        // Disable buttons during transfer
        selectFileButton.setEnabled(false);
        sendFileButton.setEnabled(false);

        // Create and execute the file sender worker
        FileSenderWorker worker = new FileSenderWorker(selectedFile, ipAddress, port);
        worker.execute();
    }

    /**
     * Adds a message to the log area.
     * 
     * @param message The message to add
     */
    private void log(String message) {
        SwingUtilities.invokeLater(() -> {
            logArea.append(message + "\n");
            // Auto-scroll to bottom
            logArea.setCaretPosition(logArea.getDocument().getLength());
        });
    }

    /**
     * SwingWorker class to handle file sending in a background thread.
     */
    private class FileSenderWorker extends SwingWorker<Void, String> {
        private final File file;
        private final String ipAddress;
        private final int port;

        public FileSenderWorker(File file, String ipAddress, int port) {
            this.file = file;
            this.ipAddress = ipAddress;
            this.port = port;
        }

        @Override
        protected Void doInBackground() {
            publish("Connecting to " + ipAddress + ":" + port + "...");

            try (
                Socket socket = new Socket(ipAddress, port);
                OutputStream outputStream = socket.getOutputStream();
                DataOutputStream dataOutputStream = new DataOutputStream(outputStream);
                FileInputStream fileInputStream = new FileInputStream(file)
            ) {
                publish("Connected successfully!");

                // Send the filename
                dataOutputStream.writeUTF(file.getName());

                // Send the file size
                long fileSize = file.length();
                dataOutputStream.writeLong(fileSize);

                publish("Sending file: " + file.getName());
                publish("File size: " + fileSize + " bytes");

                // Create a buffer for reading the file
                byte[] buffer = new byte[4096];
                int bytesRead;
                long totalBytesRead = 0;
                long lastProgressUpdate = 0;

                // Read the file and send it in chunks
                while ((bytesRead = fileInputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);

                    totalBytesRead += bytesRead;

                    // Update progress every 5% or at least every 256KB
                    long currentProgress = (totalBytesRead * 100) / fileSize;
                    if (currentProgress > lastProgressUpdate + 5 || totalBytesRead - lastProgressUpdate * fileSize / 100 > 262144) {
                        lastProgressUpdate = currentProgress;
                        publish(String.format("Progress: %d%%", currentProgress));
                    }
                }

                outputStream.flush();
                publish("File sent successfully!");

            } catch (IOException e) {
                publish("Error sending file: " + e.getMessage());
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void process(java.util.List<String> chunks) {
            for (String message : chunks) {
                log(message);
            }
        }

        @Override
        protected void done() {
            // Re-enable buttons after transfer
            selectFileButton.setEnabled(true);
            sendFileButton.setEnabled(selectedFile != null);
        }
    }

    /**
     * Searches for available receiver devices on the network.
     */
    private void searchDevices() {
        // Clear previous discoveries
        discoveredDevices.clear();

        // Disable the search button during discovery
        searchDevicesButton.setEnabled(false);

        log("Searching for receiver devices...");

        // Create and execute the discovery worker
        DeviceDiscoveryWorker worker = new DeviceDiscoveryWorker();
        worker.execute();
    }

    /**
     * Displays a dialog with discovered devices and allows the user to select one.
     */
    private void showDeviceSelectionDialog() {
        if (discoveredDevices.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "No receiver devices found. Please ensure receivers are running and try again.",
                "No Devices Found",
                JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        ReceiverDevice[] devices = discoveredDevices.toArray(new ReceiverDevice[0]);
        ReceiverDevice selectedDevice = (ReceiverDevice) JOptionPane.showInputDialog(
            this,
            "Select a receiver device:",
            "Device Selection",
            JOptionPane.QUESTION_MESSAGE,
            null,
            devices,
            devices[0]);

        if (selectedDevice != null) {
            // Update the IP and port fields with the selected device
            ipAddressField.setText(selectedDevice.ipAddress);
            portField.setText(String.valueOf(selectedDevice.port));
            log("Selected device: " + selectedDevice);
        }
    }

    /**
     * SwingWorker class to handle device discovery in a background thread.
     */
    private class DeviceDiscoveryWorker extends SwingWorker<Void, String> {
        @Override
        protected Void doInBackground() {
            try (DatagramSocket socket = new DatagramSocket()) {
                // Enable broadcast
                socket.setBroadcast(true);

                // Set timeout
                socket.setSoTimeout(DISCOVERY_TIMEOUT);

                // Create the discovery message
                byte[] sendData = "FILEBEAM_DISCOVERY".getBytes();

                // Send to broadcast address
                InetAddress broadcastAddress = InetAddress.getByName("255.255.255.255");
                DatagramPacket sendPacket = new DatagramPacket(
                    sendData, sendData.length, broadcastAddress, DISCOVERY_PORT);

                publish("Sending discovery broadcast...");
                socket.send(sendPacket);

                // Listen for responses until timeout
                byte[] receiveData = new byte[1024];

                long endTime = System.currentTimeMillis() + DISCOVERY_TIMEOUT;

                while (System.currentTimeMillis() < endTime) {
                    try {
                        // Prepare to receive a response
                        DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);

                        // Wait for a response
                        socket.receive(receivePacket);

                        // Process the response
                        String response = new String(receivePacket.getData(), 0, receivePacket.getLength());

                        // Check if it's a valid response
                        if (response.startsWith("RECEIVER_AVAILABLE|")) {
                            String[] parts = response.split("\\|");
                            if (parts.length >= 3) {
                                String deviceName = parts[1];
                                String ipAddress = receivePacket.getAddress().getHostAddress();
                                int port;

                                try {
                                    port = Integer.parseInt(parts[2]);
                                } catch (NumberFormatException e) {
                                    port = DEFAULT_PORT;
                                }

                                // Create a new device and add it to the list
                                ReceiverDevice device = new ReceiverDevice(deviceName, ipAddress, port);
                                discoveredDevices.add(device);

                                publish("Found receiver: " + device);
                            }
                        }
                    } catch (SocketTimeoutException e) {
                        // Timeout is expected, just continue
                        break;
                    }
                }

                publish("Discovery completed. Found " + discoveredDevices.size() + " receiver(s).");

            } catch (IOException e) {
                publish("Error during device discovery: " + e.getMessage());
            }

            return null;
        }

        @Override
        protected void process(java.util.List<String> chunks) {
            for (String message : chunks) {
                log(message);
            }
        }

        @Override
        protected void done() {
            // Re-enable the search button
            searchDevicesButton.setEnabled(true);

            // Show the device selection dialog
            showDeviceSelectionDialog();
        }
    }

    /**
     * Launches the FileSender GUI.
     */
    public void start() {
        SwingUtilities.invokeLater(() -> {
            setVisible(true);
        });
    }
}
