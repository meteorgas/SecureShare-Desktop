import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.Socket;

/**
 * FileSender class for selecting and sending files over a network connection.
 * Provides a Swing GUI for user interaction.
 */
public class FileSender extends JFrame {
    private static final int DEFAULT_PORT = 5050;
    private static final String DEFAULT_IP = "127.0.0.1";

    private JTextField ipAddressField;
    private JTextField portField;
    private JButton selectFileButton;
    private JButton sendFileButton;
    private JTextArea logArea;
    private JScrollPane scrollPane;
    private File selectedFile;

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
        sendFileButton.setEnabled(false); // Disable until file is selected

        buttonPanel.add(selectFileButton);
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
     * Launches the FileSender GUI.
     */
    public void start() {
        SwingUtilities.invokeLater(() -> {
            setVisible(true);
        });
    }
}
