import java.util.Scanner;

/**
 * Main class for the FileBeam application.
 * Provides a command-line interface to choose between sender and receiver modes.
 */
public class Main {
    public static void main(String[] args) {
        System.out.println("=== FileBeam - LAN File Transfer Application ===");
        System.out.println("1. Send a file");
        System.out.println("2. Receive files");
        System.out.println("Enter your choice (1 or 2):");

        Scanner scanner = new Scanner(System.in);
        String choice = scanner.nextLine().trim();

        switch (choice) {
            case "1":
                // Run as sender
                System.out.println("Enter the receiver's IP address:");
                String ipAddress = scanner.nextLine().trim();

                FileSender fileSender = new FileSender();
                fileSender.sendFile(ipAddress);
                break;

            case "2":
                // Run as receiver
                System.out.println("Enter the directory to save received files (or press Enter for default Downloads folder):");
                String saveDirectory = scanner.nextLine().trim();

                // Use default directory if none provided
                if (saveDirectory.isEmpty()) {
                    saveDirectory = System.getProperty("user.home") + "/Downloads/FileBeam";
                }

                FileReceiver fileReceiver = new FileReceiver(saveDirectory);
                fileReceiver.startReceiving();
                break;

            default:
                System.out.println("Invalid choice. Please restart the application and enter 1 or 2.");
        }

        scanner.close();
    }
}
