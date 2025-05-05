import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Paths;

/**
 * FileReceiver class for receiving files over a network connection.
 * Provides a Swing GUI for user interaction.
 */
public class FileReceiver extends JFrame {
    private static final int DEFAULT_PORT = 5050;

    private JTextField portField;
    private JTextField saveDirectoryField;
    private JButton chooseDirButton;
    private JButton startButton;
    private JButton stopButton;
    private JTextArea logArea;
    private JScrollPane scrollPane;

    private String saveDirectory;
    private ServerSocket serverSocket;
    private ReceiverThread receiverThread;
    private boolean isRunning = false;

    /**
     * Constructor that creates and initializes the GUI.
     * 
     * @param saveDirectory The directory path where files will be saved
     */
    public FileReceiver(String saveDirectory) {
        this.saveDirectory = saveDirectory;
        // Create the directory if it doesn't exist
        File directory = new File(saveDirectory);
        if (!directory.exists()) {
            directory.mkdirs();
        }

        initializeUI();
    }

    /**
     * Sets up the GUI components.
     */
    private void initializeUI() {
        setTitle("FileBeam - File Receiver");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(500, 400);
        setLocationRelativeTo(null);

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout(10, 10));
        mainPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        // Create input panel for port and save directory
        JPanel inputPanel = new JPanel(new GridLayout(2, 3, 5, 5));

        inputPanel.add(new JLabel("Listening Port:"));
        portField = new JTextField(String.valueOf(DEFAULT_PORT));
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

        // Initial log message
        log("Ready to receive files. Click 'Start Receiver' to begin listening.");
    }

    /**
     * Opens a directory chooser dialog to select a save location.
     */
    private void chooseDirectory() {
        JFileChooser dirChooser = new JFileChooser();
        dirChooser.setDialogTitle("Choose Save Directory");
        dirChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        dirChooser.setAcceptAllFileFilterUsed(false);

        if (dirChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            saveDirectory = dirChooser.getSelectedFile().getAbsolutePath();
            saveDirectoryField.setText(saveDirectory);
            log("Save directory set to: " + saveDirectory);

            // Create the directory if it doesn't exist
            File directory = new File(saveDirectory);
            if (!directory.exists()) {
                directory.mkdirs();
            }
        }
    }

    /**
     * Starts the server to listen for incoming file transfers.
     */
    private void startReceiving() {
        if (isRunning) {
            log("Receiver is already running.");
            return;
        }

        int port;
        try {
            port = Integer.parseInt(portField.getText().trim());
        } catch (NumberFormatException e) {
            log("Error: Invalid port number. Using default port " + DEFAULT_PORT);
            port = DEFAULT_PORT;
        }

        // Disable input controls
        portField.setEnabled(false);
        chooseDirButton.setEnabled(false);
        startButton.setEnabled(false);
        stopButton.setEnabled(true);

        // Start the receiver thread
        receiverThread = new ReceiverThread(port);
        receiverThread.execute();
    }

    /**
     * Stops the server and closes all connections.
     */
    private void stopReceiving() {
        if (!isRunning) {
            log("Receiver is not running.");
            return;
        }

        log("Stopping receiver...");

        // Close the server socket
        if (serverSocket != null && !serverSocket.isClosed()) {
            try {
                serverSocket.close();
            } catch (IOException e) {
                log("Error closing server socket: " + e.getMessage());
            }
        }

        // Cancel the receiver thread
        if (receiverThread != null && !receiverThread.isDone()) {
            receiverThread.cancel(true);
        }

        isRunning = false;

        // Re-enable input controls
        portField.setEnabled(true);
        chooseDirButton.setEnabled(true);
        startButton.setEnabled(true);
        stopButton.setEnabled(false);

        log("Receiver stopped.");
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
     * SwingWorker class to handle file receiving in a background thread.
     */
    private class ReceiverThread extends SwingWorker<Void, String> {
        private final int port;

        public ReceiverThread(int port) {
            this.port = port;
        }

        @Override
        protected Void doInBackground() throws Exception {
            try {
                serverSocket = new ServerSocket(port);
                isRunning = true;

                publish("File receiver started. Listening on port " + port);

                while (!isCancelled()) {
                    publish("Waiting for sender to connect...");

                    try {
                        // Accept client connection (with timeout to check for cancellation)
                        serverSocket.setSoTimeout(1000); // 1 second timeout
                        Socket clientSocket = serverSocket.accept();

                        try (
                            // Get input streams
                            InputStream inputStream = clientSocket.getInputStream();
                            DataInputStream dataInputStream = new DataInputStream(inputStream)
                        ) {
                            // Log connection
                            publish("Connection established with: " + 
                                   clientSocket.getInetAddress().getHostAddress());

                            // Read the filename
                            String fileName = dataInputStream.readUTF();

                            // Read the file size
                            long fileSize = dataInputStream.readLong();

                            publish("Receiving file: " + fileName);
                            publish("File size: " + fileSize + " bytes");

                            // Create the complete file path
                            String filePath = Paths.get(saveDirectory, fileName).toString();

                            // Create output stream to save the file
                            try (FileOutputStream fileOutputStream = new FileOutputStream(filePath)) {
                                byte[] buffer = new byte[4096];
                                int bytesRead;
                                long totalBytesRead = 0;
                                long lastProgressUpdate = 0;

                                publish("Receiving...");

                                // Read data from socket and write to file
                                while (totalBytesRead < fileSize && 
                                      (bytesRead = inputStream.read(buffer, 0, 
                                                  (int) Math.min(buffer.length, fileSize - totalBytesRead))) != -1) {

                                    if (isCancelled()) {
                                        throw new InterruptedException("File transfer cancelled");
                                    }

                                    fileOutputStream.write(buffer, 0, bytesRead);
                                    totalBytesRead += bytesRead;

                                    // Update progress every 5% or at least every 256KB
                                    long currentProgress = (totalBytesRead * 100) / fileSize;
                                    if (currentProgress > lastProgressUpdate + 5 || 
                                        totalBytesRead - lastProgressUpdate * fileSize / 100 > 262144) {
                                        lastProgressUpdate = currentProgress;
                                        publish(String.format("Progress: %d%%", currentProgress));
                                    }
                                }

                                publish("File received successfully!");
                                publish("Saved to: " + filePath);
                            }

                        } finally {
                            // Ensure client socket is closed
                            if (clientSocket != null && !clientSocket.isClosed()) {
                                clientSocket.close();
                            }
                        }

                    } catch (java.net.SocketTimeoutException e) {
                        // This is expected due to the timeout we set
                        // Just continue the loop to check for cancellation
                    } catch (IOException e) {
                        if (!isCancelled() && isRunning) {
                            publish("Error receiving file: " + e.getMessage());
                        }
                    }
                }

            } catch (IOException e) {
                if (!isCancelled()) {
                    publish("Server error: " + e.getMessage());
                }
            } finally {
                // Ensure server socket is closed
                if (serverSocket != null && !serverSocket.isClosed()) {
                    try {
                        serverSocket.close();
                    } catch (IOException e) {
                        publish("Error closing server socket: " + e.getMessage());
                    }
                }
                isRunning = false;
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
            if (!isCancelled()) {
                // Only update UI if not cancelled by user
                SwingUtilities.invokeLater(() -> {
                    portField.setEnabled(true);
                    chooseDirButton.setEnabled(true);
                    startButton.setEnabled(true);
                    stopButton.setEnabled(false);
                });
                log("Receiver stopped due to an error or completion.");
            }
        }
    }

    /**
     * Launches the FileReceiver GUI.
     */
    public void start() {
        SwingUtilities.invokeLater(() -> {
            setVisible(true);
        });
    }
}
