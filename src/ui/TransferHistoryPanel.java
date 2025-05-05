package ui;

import utils.TransferHistoryManager;
import utils.TransferRecord;

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
        
        setLayout(new BorderLayout(5, 5));
        setBorder(new EmptyBorder(5, 5, 5, 5));
        
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
        
        // Create table
        historyTable = new JTable(tableModel);
        historyTable.setFillsViewportHeight(true);
        historyTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        // Center-align all columns except the first one
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        for (int i = 1; i < historyTable.getColumnCount(); i++) {
            historyTable.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
        }
        
        // Set column widths
        historyTable.getColumnModel().getColumn(0).setPreferredWidth(200); // File name
        historyTable.getColumnModel().getColumn(1).setPreferredWidth(150); // Date & Time
        historyTable.getColumnModel().getColumn(2).setPreferredWidth(80);  // Size
        historyTable.getColumnModel().getColumn(3).setPreferredWidth(80);  // Direction
        
        // Add table to a scroll pane
        JScrollPane scrollPane = new JScrollPane(historyTable);
        add(scrollPane, BorderLayout.CENTER);
        
        // Create button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton clearButton = new JButton("Clear History");
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