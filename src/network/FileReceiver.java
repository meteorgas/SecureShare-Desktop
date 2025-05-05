package network;

import utils.Config;
import utils.FileUtils;

import javax.swing.*;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Paths;
import java.util.function.Consumer;

/**
 * Handles the network operations for receiving files over TCP.
 */
public class FileReceiver {
    private final Consumer<String> logCallback;
    private final Consumer<Integer> progressCallback;
    private final String saveDirectory;
    
    private ServerSocket serverSocket;
    private ReceiverThread receiverThread;
    private boolean isRunning = false;
    
    /**
     * Creates a new FileReceiver with callbacks for logging and progress updates.
     * 
     * @param saveDirectory The directory where received files will be saved
     * @param logCallback Callback for log messages
     * @param progressCallback Callback for progress updates
     */
    public FileReceiver(String saveDirectory, Consumer<String> logCallback, Consumer<Integer> progressCallback) {
        this.saveDirectory = saveDirectory;
        this.logCallback = logCallback;
        this.progressCallback = progressCallback;
        
        // Ensure the save directory exists
        FileUtils.ensureDirectoryExists(saveDirectory);
    }
    
    /**
     * Starts the receiver to listen for incoming file transfers.
     * 
     * @param port The port to listen on
     * @return true if the receiver was started successfully, false otherwise
     */
    public boolean start(int port) {
        if (isRunning) {
            log("Receiver is already running.");
            return false;
        }
        
        try {
            // Start the receiver thread
            receiverThread = new ReceiverThread(port);
            receiverThread.execute();
            return true;
        } catch (Exception e) {
            log("Error starting receiver: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Stops the receiver and closes all connections.
     */
    public void stop() {
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
        log("Receiver stopped.");
    }
    
    /**
     * Checks if the receiver is currently running.
     * 
     * @return true if the receiver is running, false otherwise
     */
    public boolean isRunning() {
        return isRunning;
    }
    
    /**
     * Logs a message using the log callback.
     * 
     * @param message The message to log
     */
    private void log(String message) {
        if (logCallback != null) {
            logCallback.accept(message);
        }
    }
    
    /**
     * Updates progress using the progress callback.
     * 
     * @param percentage The progress percentage (0-100)
     */
    private void updateProgress(int percentage) {
        if (progressCallback != null) {
            progressCallback.accept(percentage);
        }
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
                                byte[] buffer = new byte[Config.BUFFER_SIZE];
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

                                    // Update progress every X% or at least every Y bytes
                                    long currentProgress = (totalBytesRead * 100) / fileSize;
                                    if (currentProgress > lastProgressUpdate + Config.PROGRESS_UPDATE_PERCENTAGE || 
                                        totalBytesRead - lastProgressUpdate * fileSize / 100 > Config.PROGRESS_UPDATE_BYTES) {
                                        lastProgressUpdate = currentProgress;
                                        updateProgress((int)currentProgress);
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
            // Reset progress when done
            updateProgress(0);
        }
    }
}