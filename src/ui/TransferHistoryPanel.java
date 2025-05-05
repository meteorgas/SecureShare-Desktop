package ui;

import utils.TransferHistoryManager;
import utils.TransferRecord;
import utils.UIStyleUtils;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * A panel that displays the history of file transfers.
 */
public class TransferHistoryPanel extends JPanel {
    private final TransferHistoryManager historyManager;
    private final JTable historyTable;
    private final DefaultTableModel tableModel;

    /**
     * Creates a new TransferHistoryPanel.
     * 
     * @param historyManager The history manager to use
     */
    public TransferHistoryPanel(TransferHistoryManager historyManager) {
        this.historyManager = historyManager;

        setLayout(new BorderLayout(8, 8));
        setBorder(new EmptyBorder(8, 8, 8, 8));

        // Create table model with non-editable cells
        tableModel = new DefaultTableModel() {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        // Add columns
        tableModel.addColumn("File Name");
        tableModel.addColumn("Date & Time");
        tableModel.addColumn("Size");
        tableModel.addColumn("Direction");

        // Create table with styling
        historyTable = new JTable(tableModel);
        historyTable.setFillsViewportHeight(true);
        historyTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // Apply the new table styling from Task 9
        UIStyleUtils.styleTable(historyTable);

        // Set column widths with equal spacing as per Task 9
        int totalWidth = 530; // Approximate width of the table
        historyTable.getColumnModel().getColumn(0).setPreferredWidth((int)(totalWidth * 0.4)); // File name (40%)
        historyTable.getColumnModel().getColumn(1).setPreferredWidth((int)(totalWidth * 0.3)); // Date & Time (30%)
        historyTable.getColumnModel().getColumn(2).setPreferredWidth((int)(totalWidth * 0.15)); // Size (15%)
        historyTable.getColumnModel().getColumn(3).setPreferredWidth((int)(totalWidth * 0.15)); // Direction (15%)

        // Center-align all columns except the first one
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        for (int i = 1; i < historyTable.getColumnCount(); i++) {
            historyTable.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
        }

        // Add table to a scroll pane with styled border using SECONDARY_GRAY
        JScrollPane scrollPane = new JScrollPane(historyTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(UIStyleUtils.SECONDARY_GRAY, 1));
        scrollPane.getViewport().setBackground(Color.WHITE); // Ensure viewport background is white
        add(scrollPane, BorderLayout.CENTER);

        // Create button panel with spacing
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(8, 0, 0, 0));

        JButton clearButton = new JButton("Clear History");
        UIStyleUtils.styleButton(clearButton);
        clearButton.addActionListener(e -> historyManager.clearHistory());

        buttonPanel.add(clearButton);
        add(buttonPanel, BorderLayout.SOUTH);

        // Register as a listener for history changes
        historyManager.addHistoryListener(this::updateHistoryTable);

        // Initial population of the table
        updateHistoryTable(historyManager.getTransferHistory());
    }

    /**
     * Updates the history table with the current list of transfer records.
     * 
     * @param records The list of transfer records
     */
    private void updateHistoryTable(List<TransferRecord> records) {
        // Clear the table
        tableModel.setRowCount(0);

        // Add rows for each record (newest first)
        for (int i = records.size() - 1; i >= 0; i--) {
            TransferRecord record = records.get(i);
            tableModel.addRow(new Object[]{
                    record.getFileName(),
                    record.getFormattedTimestamp(),
                    record.getFormattedFileSize(),
                    record.getDirectionString()
            });
        }
    }
}
