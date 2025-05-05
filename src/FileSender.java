import javax.swing.*;
import java.io.*;
import java.net.Socket;

/**
 * FileSender class for selecting and sending files over a network connection.
 */
public class FileSender {
    private static final int PORT = 5050;
    
    /**
     * Allows user to select a file and send it to a specified IP address.
     * 
     * @param ipAddress The IP address of the receiver
     */
    public void sendFile(String ipAddress) {
        // Create a file chooser
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Select a file to send");
        
        // Show the file chooser dialog
        int result = fileChooser.showOpenDialog(null);
        
        // If a file was selected
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            System.out.println("Selected file: " + selectedFile.getAbsolutePath());
            
            try (
                // Create socket connection to receiver
                Socket socket = new Socket(ipAddress, PORT);
                // Get output streams
                OutputStream outputStream = socket.getOutputStream();
                DataOutputStream dataOutputStream = new DataOutputStream(outputStream);
                // Create file input stream to read the file
                FileInputStream fileInputStream = new FileInputStream(selectedFile)
            ) {
                // Send the filename first
                dataOutputStream.writeUTF(selectedFile.getName());
                
                // Send the file size
                dataOutputStream.writeLong(selectedFile.length());
                
                // Create a buffer for reading the file
                byte[] buffer = new byte[4096];
                int bytesRead;
                
                System.out.println("Sending file: " + selectedFile.getName());
                
                // Read the file and send it in chunks
                while ((bytesRead = fileInputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }
                
                outputStream.flush();
                System.out.println("File sent successfully!");
                
            } catch (IOException e) {
                System.err.println("Error sending file: " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            System.out.println("File selection cancelled.");
        }
    }
}