package utils;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Represents a record of a file transfer (sent or received).
 * This class is used to store information about file transfers for history tracking.
 */
public class TransferRecord implements Serializable {
    private static final long serialVersionUID = 1L;
    
    public enum Direction {
        SENT, RECEIVED
    }
    
    private final String fileName;
    private final long timestamp;
    private final long fileSize;
    private final Direction direction;
    
    /**
     * Creates a new transfer record.
     * 
     * @param fileName The name of the transferred file
     * @param fileSize The size of the file in bytes
     * @param direction The direction of the transfer (SENT or RECEIVED)
     */
    public TransferRecord(String fileName, long fileSize, Direction direction) {
        this.fileName = fileName;
        this.timestamp = System.currentTimeMillis();
        this.fileSize = fileSize;
        this.direction = direction;
    }
    
    /**
     * Gets the name of the transferred file.
     * 
     * @return The file name
     */
    public String getFileName() {
        return fileName;
    }
    
    /**
     * Gets the timestamp when the transfer occurred.
     * 
     * @return The timestamp in milliseconds since epoch
     */
    public long getTimestamp() {
        return timestamp;
    }
    
    /**
     * Gets the formatted timestamp as a string.
     * 
     * @return The formatted date and time
     */
    public String getFormattedTimestamp() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return sdf.format(new Date(timestamp));
    }
    
    /**
     * Gets the size of the transferred file.
     * 
     * @return The file size in bytes
     */
    public long getFileSize() {
        return fileSize;
    }
    
    /**
     * Gets the formatted file size as a string with appropriate units.
     * 
     * @return The formatted file size (e.g., "1.23 MB")
     */
    public String getFormattedFileSize() {
        if (fileSize < 1024) {
            return fileSize + " B";
        } else if (fileSize < 1024 * 1024) {
            return String.format("%.2f KB", fileSize / 1024.0);
        } else if (fileSize < 1024 * 1024 * 1024) {
            return String.format("%.2f MB", fileSize / (1024.0 * 1024.0));
        } else {
            return String.format("%.2f GB", fileSize / (1024.0 * 1024.0 * 1024.0));
        }
    }
    
    /**
     * Gets the direction of the transfer.
     * 
     * @return The transfer direction (SENT or RECEIVED)
     */
    public Direction getDirection() {
        return direction;
    }
    
    /**
     * Gets the direction as a string.
     * 
     * @return "Sent" or "Received"
     */
    public String getDirectionString() {
        return direction == Direction.SENT ? "Sent" : "Received";
    }
    
    @Override
    public String toString() {
        return String.format("%s | %s | %s | %s", 
                getFileName(), 
                getFormattedTimestamp(), 
                getFormattedFileSize(), 
                getDirectionString());
    }
}