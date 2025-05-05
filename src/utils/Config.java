package utils;

/**
 * Configuration constants for the FileBeam application.
 */
public class Config {
    // Network configuration
    public static final int DEFAULT_PORT = 5050;
    public static final String DEFAULT_IP = "127.0.0.1";
    public static final int DISCOVERY_PORT = 8888;
    public static final int DISCOVERY_TIMEOUT = 3000; // 3 seconds

    // File transfer configuration
    public static final int BUFFER_SIZE = 4096;
    public static final int PROGRESS_UPDATE_PERCENTAGE = 5;
    public static final int PROGRESS_UPDATE_BYTES = 262144; // 256KB

    // Discovery protocol messages
    public static final String DISCOVERY_REQUEST = "FILEBEAM_DISCOVERY";
    public static final String DISCOVERY_RESPONSE_PREFIX = "RECEIVER_AVAILABLE|";

    // Default save directory
    public static final String DEFAULT_SAVE_DIRECTORY = System.getProperty("user.home") + "/Downloads/FileBeam";

    // UI configuration
    public static final String APP_TITLE = "FileBeam - LAN File Transfer Application";
    public static final String SENDER_TITLE = "FileBeam - File Sender";
    public static final String RECEIVER_TITLE = "FileBeam - File Receiver";

    // History configuration
    public static final String HISTORY_FILE = "transfer_history.csv";
}
