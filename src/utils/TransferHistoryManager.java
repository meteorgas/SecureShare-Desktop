package utils;

import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

/**
 * Manages the history of file transfers and handles persistence.
 */
public class TransferHistoryManager {
    private static final String HISTORY_FILE = Config.HISTORY_FILE;
    private final List<TransferRecord> transferHistory = new ArrayList<>();
    private final List<Consumer<List<TransferRecord>>> historyListeners = new ArrayList<>();

    /**
     * Creates a new TransferHistoryManager and loads any existing history.
     */
    public TransferHistoryManager() {
        loadHistory();
    }

    /**
     * Adds a new transfer record to the history.
     * 
     * @param record The transfer record to add
     */
    public void addTransferRecord(TransferRecord record) {
        transferHistory.add(record);
        saveHistory();
        notifyListeners();
    }

    /**
     * Gets an unmodifiable view of the transfer history.
     * 
     * @return The list of transfer records
     */
    public List<TransferRecord> getTransferHistory() {
        return Collections.unmodifiableList(transferHistory);
    }

    /**
     * Clears all transfer history.
     */
    public void clearHistory() {
        transferHistory.clear();
        saveHistory();
        notifyListeners();
    }

    /**
     * Adds a listener that will be notified when the history changes.
     * 
     * @param listener The listener to add
     */
    public void addHistoryListener(Consumer<List<TransferRecord>> listener) {
        historyListeners.add(listener);
    }

    /**
     * Removes a previously added history listener.
     * 
     * @param listener The listener to remove
     */
    public void removeHistoryListener(Consumer<List<TransferRecord>> listener) {
        historyListeners.remove(listener);
    }

    /**
     * Notifies all listeners that the history has changed.
     */
    private void notifyListeners() {
        for (Consumer<List<TransferRecord>> listener : historyListeners) {
            listener.accept(Collections.unmodifiableList(transferHistory));
        }
    }

    /**
     * Saves the transfer history to a CSV file.
     */
    private void saveHistory() {
        try (PrintWriter writer = new PrintWriter(new FileWriter(HISTORY_FILE))) {
            // Write CSV header
            writer.println("FileName,Timestamp,FileSize,Direction");

            // Write each record
            for (TransferRecord record : transferHistory) {
                writer.printf("%s,%d,%d,%s%n",
                        record.getFileName(),
                        record.getTimestamp(),
                        record.getFileSize(),
                        record.getDirection().name());
            }
        } catch (IOException e) {
            System.err.println("Error saving transfer history: " + e.getMessage());
        }
    }

    /**
     * Loads the transfer history from a CSV file.
     */
    private void loadHistory() {
        File historyFile = new File(HISTORY_FILE);
        if (!historyFile.exists()) {
            return;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(historyFile))) {
            // Skip header line
            String line = reader.readLine();

            // Read records
            while ((line = reader.readLine()) != null) {
                try {
                    String[] parts = line.split(",");
                    if (parts.length >= 4) {
                        String fileName = parts[0];
                        long timestamp = Long.parseLong(parts[1]);
                        long fileSize = Long.parseLong(parts[2]);
                        TransferRecord.Direction direction = TransferRecord.Direction.valueOf(parts[3]);

                        // Create record with custom timestamp
                        TransferRecord record = new TransferRecord(fileName, fileSize, direction) {
                            @Override
                            public long getTimestamp() {
                                return timestamp;
                            }
                        };

                        transferHistory.add(record);
                    }
                } catch (Exception e) {
                    System.err.println("Error parsing history line: " + line);
                }
            }
        } catch (IOException e) {
            System.err.println("Error loading transfer history: " + e.getMessage());
        }
    }
}
