import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Main class for the FileBeam application.
 * Provides a graphical interface to choose between sender and receiver modes.
 */
public class Main extends JFrame {

    /**
     * Constructor that creates and initializes the GUI.
     */
    public Main() {
        initializeUI();
    }

    /**
     * Sets up the GUI components.
     */
    private void initializeUI() {
        setTitle("FileBeam - LAN File Transfer Application");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(400, 200);
        setLocationRelativeTo(null);

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout(10, 10));
        mainPanel.setBorder(new EmptyBorder(20, 20, 20, 20));

        // Create title label
        JLabel titleLabel = new JLabel("FileBeam - LAN File Transfer Application");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 16));
        titleLabel.setHorizontalAlignment(JLabel.CENTER);

        // Create button panel
        JPanel buttonPanel = new JPanel(new GridLayout(2, 1, 10, 10));

        JButton senderButton = new JButton("Send a File");
        JButton receiverButton = new JButton("Receive Files");

        buttonPanel.add(senderButton);
        buttonPanel.add(receiverButton);

        // Add components to main panel
        mainPanel.add(titleLabel, BorderLayout.NORTH);
        mainPanel.add(buttonPanel, BorderLayout.CENTER);

        // Add main panel to frame
        add(mainPanel);

        // Add action listeners
        senderButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                launchSender();
            }
        });

        receiverButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                launchReceiver();
            }
        });
    }

    /**
     * Launches the FileSender GUI.
     */
    private void launchSender() {
        FileSender fileSender = new FileSender();
        fileSender.start();
    }

    /**
     * Launches the FileReceiver GUI.
     */
    private void launchReceiver() {
        // Use default directory if none provided
        String saveDirectory = System.getProperty("user.home") + "/Downloads/FileBeam";

        FileReceiver fileReceiver = new FileReceiver(saveDirectory);
        fileReceiver.start();
    }

    /**
     * Main method to start the application.
     * 
     * @param args Command line arguments (not used)
     */
    public static void main(String[] args) {
        // Set look and feel to system default
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Launch the application
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                Main app = new Main();
                app.setVisible(true);
            }
        });
    }
}
