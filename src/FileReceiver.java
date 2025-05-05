import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Paths;

/**
 * FileReceiver class for receiving files over a network connection.
 */
public class FileReceiver {
    private static final int PORT = 5050;
    private final String saveDirectory;
    
    /**
     * Constructor that sets the directory where received files will be saved.
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
    }
    
    /**
     * Starts the server to listen for incoming file transfers.
     */
    public void startReceiving() {
        System.out.println("File receiver started. Listening on port " + PORT);
        
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            while (true) {
                System.out.println("Waiting for sender to connect...");
                
                try (
                    // Accept client connection
                    Socket clientSocket = serverSocket.accept();
                    // Get input streams
                    InputStream inputStream = clientSocket.getInputStream();
                    DataInputStream dataInputStream = new DataInputStream(inputStream)
                ) {
                    // Log connection
                    System.out.println("Connection established with: " + 
                                      clientSocket.getInetAddress().getHostAddress());
                    
                    // Read the filename
                    String fileName = dataInputStream.readUTF();
                    
                    // Read the file size
                    long fileSize = dataInputStream.readLong();
                    
                    System.out.println("Receiving file: " + fileName);
                    System.out.println("File size: " + fileSize + " bytes");
                    
                    // Create the complete file path
                    String filePath = Paths.get(saveDirectory, fileName).toString();
                    
                    // Create output stream to save the file
                    try (FileOutputStream fileOutputStream = new FileOutputStream(filePath)) {
                        byte[] buffer = new byte[4096];
                        int bytesRead;
                        long totalBytesRead = 0;
                        
                        System.out.println("Receiving...");
                        
                        // Read data from socket and write to file
                        while (totalBytesRead < fileSize && 
                              (bytesRead = inputStream.read(buffer, 0, 
                                          (int) Math.min(buffer.length, fileSize - totalBytesRead))) != -1) {
                            fileOutputStream.write(buffer, 0, bytesRead);
                            totalBytesRead += bytesRead;
                            
                            // Print progress (optional)
                            if (fileSize > 0) {
                                double progress = (double) totalBytesRead / fileSize * 100;
                                System.out.printf("Progress: %.1f%%\r", progress);
                            }
                        }
                        
                        System.out.println("\nFile received successfully!");
                        System.out.println("Saved to: " + filePath);
                    }
                    
                } catch (IOException e) {
                    System.err.println("Error receiving file: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        } catch (IOException e) {
            System.err.println("Server error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}