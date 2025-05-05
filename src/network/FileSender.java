package network;

import utils.Config;
import utils.TransferHistoryManager;
import utils.TransferRecord;

import javax.swing.*;
import java.io.*;
import java.net.Socket;
import java.util.function.Consumer;

/**
 * Handles the network operations for sending files over TCP.
 */
public class FileSender {
    private final Consumer<String> logCallback;
    private final Consumer<Integer> progressCallback;
    private final TransferHistoryManager historyManager;

    /**
     * Creates a new FileSender with callbacks for logging and progress updates.
     * 
     * @param logCallback Callback for log messages
     * @param progressCallback Callback for progress updates
     * @param historyManager Manager for tracking transfer history
     */
    public FileSender(Consumer<String> logCallback, Consumer<Integer> progressCallback, TransferHistoryManager historyManager) {
        this.logCallback = logCallback;
        this.progressCallback = progressCallback;
        this.historyManager = historyManager;
    }

    /**
     * Sends a file to a receiver.
     * 
     * @param file The file to send
     * @param ipAddress The IP address of the receiver
     * @param port The port of the receiver
     */
    public void sendFile(File file, String ipAddress, int port) {
        // Create and execute the file sender worker
        FileSenderWorker worker = new FileSenderWorker(file, ipAddress, port);
        worker.execute();
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
                byte[] buffer = new byte[Config.BUFFER_SIZE];
                int bytesRead;
                long totalBytesRead = 0;
                long lastProgressUpdate = 0;

                // Read the file and send it in chunks
                while ((bytesRead = fileInputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);

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

                outputStream.flush();
                publish("File sent successfully!");

                // Record the transfer in history
                if (historyManager != null) {
                    TransferRecord record = new TransferRecord(file.getName(), fileSize, TransferRecord.Direction.SENT);
                    historyManager.addTransferRecord(record);
                }

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
            // Reset progress when done
            updateProgress(0);
        }
    }
}
