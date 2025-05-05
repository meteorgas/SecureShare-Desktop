package utils;

import javax.swing.*;
import java.io.File;

/**
 * Utility methods for file operations in the FileBeam application.
 */
public class FileUtils {
    
    /**
     * Opens a file chooser dialog to select a file.
     * 
     * @param parent The parent component for the dialog
     * @param title The dialog title
     * @return The selected file, or null if selection was cancelled
     */
    public static File selectFile(JComponent parent, String title) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle(title);

        int result = fileChooser.showOpenDialog(parent);

        if (result == JFileChooser.APPROVE_OPTION) {
            return fileChooser.getSelectedFile();
        }
        
        return null;
    }
    
    /**
     * Opens a directory chooser dialog to select a directory.
     * 
     * @param parent The parent component for the dialog
     * @param title The dialog title
     * @return The selected directory path, or null if selection was cancelled
     */
    public static String selectDirectory(JComponent parent, String title) {
        JFileChooser dirChooser = new JFileChooser();
        dirChooser.setDialogTitle(title);
        dirChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        dirChooser.setAcceptAllFileFilterUsed(false);

        if (dirChooser.showOpenDialog(parent) == JFileChooser.APPROVE_OPTION) {
            return dirChooser.getSelectedFile().getAbsolutePath();
        }
        
        return null;
    }
    
    /**
     * Ensures a directory exists, creating it if necessary.
     * 
     * @param directoryPath The path of the directory to check/create
     * @return true if the directory exists or was created successfully, false otherwise
     */
    public static boolean ensureDirectoryExists(String directoryPath) {
        File directory = new File(directoryPath);
        if (!directory.exists()) {
            return directory.mkdirs();
        }
        return true;
    }
    
    /**
     * Gets the file extension from a filename.
     * 
     * @param filename The filename to extract the extension from
     * @return The file extension (without the dot) or an empty string if no extension
     */
    public static String getFileExtension(String filename) {
        int lastDotIndex = filename.lastIndexOf('.');
        if (lastDotIndex > 0 && lastDotIndex < filename.length() - 1) {
            return filename.substring(lastDotIndex + 1).toLowerCase();
        }
        return "";
    }
}