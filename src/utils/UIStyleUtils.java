package utils;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;

/**
 * Utility class for UI styling in the SecureShare application.
 * Provides methods for consistent styling across the application.
 */
public class UIStyleUtils {
    // Font constants
    public static final Font TITLE_FONT = new Font(Font.SANS_SERIF, Font.BOLD, 14);
    public static final Font SECTION_FONT = new Font(Font.SANS_SERIF, Font.BOLD, 12);
    public static final Font REGULAR_FONT = new Font(Font.SANS_SERIF, Font.PLAIN, 12);

    // Color palette as specified in design specs
    public static final Color PRIMARY_BLUE = new Color(0x4285F4); // #4285F4 - Main buttons, highlights
    public static final Color LIGHT_BLUE_BG = new Color(0xE8F0FE); // #E8F0FE - Section backgrounds
    public static final Color SECONDARY_GRAY = new Color(0xE0E0E0); // #E0E0E0 - Borders, inactive states
    public static final Color TEXT_PRIMARY = new Color(0x202124); // #202124 - Titles, main text
    public static final Color TEXT_SECONDARY = new Color(0x5F6368); // #5F6368 - Helper text, hints

    // Button colors for normal state
    public static final Color BUTTON_BACKGROUND = PRIMARY_BLUE;
    public static final Color BUTTON_FOREGROUND = Color.WHITE;

    // Button hover color
    public static final Color BUTTON_HOVER = new Color(0x6EA5F8); // #6EA5F8

    /**
     * Creates a titled border with improved typography.
     * 
     * @param title The title text
     * @return A styled titled border
     */
    public static Border createSectionBorder(String title) {
        // Create a titled border with increased padding (12-16px) as per Task 6
        TitledBorder titledBorder = BorderFactory.createTitledBorder(
            BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(SECONDARY_GRAY, 1),
                BorderFactory.createEmptyBorder(14, 14, 14, 14) // Increased padding to 14px
            ),
            title
        );

        // Use slightly larger font size for section headers as per Task 6
        Font sectionHeaderFont = new Font(Font.SANS_SERIF, Font.BOLD, 14);
        titledBorder.setTitleFont(sectionHeaderFont);
        titledBorder.setTitleColor(TEXT_PRIMARY); // #202124 for titles as per color palette
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
        button.setBackground(PRIMARY_BLUE);
        button.setForeground(Color.WHITE);

        // Add rounded corners using a custom UI
        button.setUI(new RoundedButtonUI());
    }

    /**
     * Applies styling to a text field.
     * 
     * @param textField The text field to style
     */
    public static void styleTextField(JTextField textField) {
        // Use consistent font style as per Task 8
        textField.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));
        textField.setForeground(TEXT_PRIMARY);

        // Consistent padding (12px) as per Task 6
        textField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(SECONDARY_GRAY),
            BorderFactory.createEmptyBorder(12, 12, 12, 12)
        ));
    }

    /**
     * Applies styling to a text area.
     * 
     * @param textArea The text area to style
     */
    public static void styleTextArea(JTextArea textArea) {
        // Use consistent font style as per Task 8
        textArea.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));
        textArea.setForeground(TEXT_PRIMARY);

        // Consistent padding (12px) as per Task 6
        textArea.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(SECONDARY_GRAY),
            BorderFactory.createEmptyBorder(12, 12, 12, 12)
        ));
    }

    /**
     * Applies styling to a label.
     * 
     * @param label The label to style
     */
    public static void styleLabel(JLabel label) {
        // Use consistent font style as per Task 8
        label.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));
        label.setForeground(TEXT_PRIMARY);

        // Left-align labels as per Task 8
        label.setHorizontalAlignment(SwingConstants.LEFT);
    }

    /**
     * Applies styling to a helper text label.
     * 
     * @param label The helper text label to style
     */
    public static void styleHelperLabel(JLabel label) {
        // Use secondary text color for helper text as per Task 8
        label.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        label.setForeground(TEXT_SECONDARY);
        label.setHorizontalAlignment(SwingConstants.CENTER);
    }

    /**
     * Applies styling to a section title label.
     * 
     * @param label The label to style as a section title
     */
    public static void styleSectionLabel(JLabel label) {
        label.setFont(SECTION_FONT);
        label.setForeground(TEXT_PRIMARY);
    }

    /**
     * Applies styling to a table according to Task 9 requirements.
     * 
     * @param table The table to style
     */
    public static void styleTable(JTable table) {
        // Use consistent font styles
        table.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));
        table.getTableHeader().setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));
        table.getTableHeader().setForeground(TEXT_PRIMARY);

        // Increase row height for better readability
        table.setRowHeight(30);

        // Remove grid lines for cleaner look
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(8, 8)); // Add spacing between cells

        // Make columns equally spaced and consistently aligned
        for (int i = 0; i < table.getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setPreferredWidth(table.getWidth() / table.getColumnCount());
        }

        // Alternating row colors (white and #E8F0FE) as per Task 9
        table.setDefaultRenderer(Object.class, new javax.swing.table.DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

                if (isSelected) {
                    // Use light selection color instead of default blue highlight
                    c.setBackground(new Color(0xD2E3FC)); // Lighter version of LIGHT_BLUE_BG
                    c.setForeground(TEXT_PRIMARY);
                } else {
                    // Alternating row colors
                    c.setBackground(row % 2 == 0 ? Color.WHITE : LIGHT_BLUE_BG);
                    c.setForeground(TEXT_PRIMARY);
                }

                // Remove cell border focus
                ((JComponent) c).setBorder(BorderFactory.createEmptyBorder(2, 8, 2, 8));

                return c;
            }
        });

        // Style the table header
        table.getTableHeader().setDefaultRenderer(new javax.swing.table.DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

                c.setBackground(Color.WHITE);
                c.setForeground(TEXT_PRIMARY);
                ((JComponent) c).setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 0, 1, 0, SECONDARY_GRAY),
                    BorderFactory.createEmptyBorder(8, 8, 8, 8)
                ));

                return c;
            }
        });
    }

    /**
     * Custom ButtonUI implementation for rounded corners.
     */
    private static class RoundedButtonUI extends javax.swing.plaf.basic.BasicButtonUI {
        // Rounded corners with 6-8px radius as per Task 7
        private static final int ARC_SIZE = 8;

        @Override
        public void paint(Graphics g, JComponent c) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            AbstractButton button = (AbstractButton) c;
            ButtonModel model = button.getModel();

            // Determine button state and adjust color according to Task 7
            Color background = button.getBackground();
            if (model.isPressed()) {
                background = background.darker();
            } else if (model.isRollover()) {
                // Use specified hover color #6EA5F8
                background = BUTTON_HOVER;
            }

            // Draw rounded rectangle background
            g2.setColor(background);
            g2.fill(new RoundRectangle2D.Double(0, 0, c.getWidth(), c.getHeight(), ARC_SIZE, ARC_SIZE));

            // Paint the text and icon
            super.paint(g2, c);
            g2.dispose();
        }

        @Override
        public Dimension getPreferredSize(JComponent c) {
            // Ensure consistent button height as per Task 7
            Dimension d = super.getPreferredSize(c);
            d.height = Math.max(d.height, 32); // Minimum height of 32px
            return d;
        }
    }
}
