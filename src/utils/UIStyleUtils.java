package utils;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;

/**
 * Utility class for UI styling in the FileBeam application.
 * Provides methods for consistent styling across the application.
 */
public class UIStyleUtils {
    // Font constants
    public static final Font TITLE_FONT = new Font(Font.SANS_SERIF, Font.BOLD, 14);
    public static final Font SECTION_FONT = new Font(Font.SANS_SERIF, Font.BOLD, 12);
    public static final Font REGULAR_FONT = new Font(Font.SANS_SERIF, Font.PLAIN, 12);
    
    // Color constants
    public static final Color SECTION_TITLE_COLOR = new Color(44, 62, 80);
    public static final Color BUTTON_BACKGROUND = new Color(52, 152, 219);
    public static final Color BUTTON_TEXT = Color.WHITE;
    
    /**
     * Creates a titled border with improved typography.
     * 
     * @param title The title text
     * @return A styled titled border
     */
    public static Border createSectionBorder(String title) {
        TitledBorder titledBorder = BorderFactory.createTitledBorder(
            BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(189, 195, 199), 1),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)
            ),
            title
        );
        titledBorder.setTitleFont(SECTION_FONT);
        titledBorder.setTitleColor(SECTION_TITLE_COLOR);
        titledBorder.setTitleJustification(TitledBorder.LEFT);
        titledBorder.setTitlePosition(TitledBorder.TOP);
        
        return titledBorder;
    }
    
    /**
     * Applies rounded corners and styling to a button.
     * 
     * @param button The button to style
     */
    public static void styleButton(JButton button) {
        button.setFont(REGULAR_FONT);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setContentAreaFilled(false);
        button.setOpaque(true);
        button.setBackground(BUTTON_BACKGROUND);
        button.setForeground(BUTTON_TEXT);
        
        // Add rounded corners using a custom UI
        button.setUI(new RoundedButtonUI());
    }
    
    /**
     * Applies styling to a text field.
     * 
     * @param textField The text field to style
     */
    public static void styleTextField(JTextField textField) {
        textField.setFont(REGULAR_FONT);
        textField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(189, 195, 199)),
            BorderFactory.createEmptyBorder(5, 5, 5, 5)
        ));
    }
    
    /**
     * Applies styling to a text area.
     * 
     * @param textArea The text area to style
     */
    public static void styleTextArea(JTextArea textArea) {
        textArea.setFont(REGULAR_FONT);
        textArea.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(189, 195, 199)),
            BorderFactory.createEmptyBorder(5, 5, 5, 5)
        ));
    }
    
    /**
     * Applies styling to a label.
     * 
     * @param label The label to style
     */
    public static void styleLabel(JLabel label) {
        label.setFont(REGULAR_FONT);
    }
    
    /**
     * Applies styling to a section title label.
     * 
     * @param label The label to style as a section title
     */
    public static void styleSectionLabel(JLabel label) {
        label.setFont(SECTION_FONT);
        label.setForeground(SECTION_TITLE_COLOR);
    }
    
    /**
     * Custom ButtonUI implementation for rounded corners.
     */
    private static class RoundedButtonUI extends javax.swing.plaf.basic.BasicButtonUI {
        private static final int ARC_SIZE = 10;
        
        @Override
        public void paint(Graphics g, JComponent c) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            
            AbstractButton button = (AbstractButton) c;
            ButtonModel model = button.getModel();
            
            // Determine button state and adjust color
            Color background = button.getBackground();
            if (model.isPressed()) {
                background = background.darker();
            } else if (model.isRollover()) {
                background = background.brighter();
            }
            
            // Draw rounded rectangle background
            g2.setColor(background);
            g2.fill(new RoundRectangle2D.Double(0, 0, c.getWidth(), c.getHeight(), ARC_SIZE, ARC_SIZE));
            
            // Paint the text and icon
            super.paint(g2, c);
            g2.dispose();
        }
    }
}