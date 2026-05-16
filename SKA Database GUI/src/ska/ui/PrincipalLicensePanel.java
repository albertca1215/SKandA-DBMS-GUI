package ska.ui;

import ska.db.*;
import ska.model.*;
import javax.swing.*;
import java.awt.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Panel for managing Principal licenses by state.
 * Principals must be licensed in the state where they are responsible for a project.
 */
public class PrincipalLicensePanel extends JPanel {

    private MainFrame parent;
    private EmployeeDAO employeeDAO;
    private LocationDAO locationDAO;
    
    private JComboBox<Employee> principalCombo;
    private JComboBox<String> stateCombo;
    private JTable licenseTable;
    private JButton addBtn, deleteBtn, refreshBtn;

    public PrincipalLicensePanel(MainFrame parent) {
        this.parent = parent;
        this.employeeDAO = new EmployeeDAO();
        this.locationDAO = new LocationDAO();
        
        setLayout(new BorderLayout());
        buildUI();
        refreshPrincipalLicenses();
    }

    private void buildUI() {
        // ── Input Panel ────────────────────────────────────────────
        JPanel inputPanel = new JPanel(new GridBagLayout());
        inputPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createEmptyBorder(10, 10, 10, 10),
            BorderFactory.createTitledBorder("Manage Principal Licenses")
        ));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        
        // Principal Selection
        gbc.gridx = 0; gbc.gridy = 0;
        inputPanel.add(new JLabel("Principal:"), gbc);
        gbc.gridx = 1;
        principalCombo = new JComboBox<>();
        loadPrincipals();
        principalCombo.addActionListener(e -> refreshLicenseTable());
        inputPanel.add(principalCombo, gbc);
        
        // State Selection
        gbc.gridx = 0; gbc.gridy = 1;
        inputPanel.add(new JLabel("State:"), gbc);
        gbc.gridx = 1;
        stateCombo = new JComboBox<>();
        loadStates();
        inputPanel.add(stateCombo, gbc);
        
        // Buttons
        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 2;
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        
        addBtn = new JButton("Add License");
        addBtn.addActionListener(e -> addLicense());
        buttonPanel.add(addBtn);
        
        deleteBtn = new JButton("Delete License");
        deleteBtn.addActionListener(e -> deleteLicense());
        buttonPanel.add(deleteBtn);
        
        refreshBtn = new JButton("Refresh");
        refreshBtn.addActionListener(e -> refreshPrincipalLicenses());
        buttonPanel.add(refreshBtn);
        
        inputPanel.add(buttonPanel, gbc);
        
        // ── Table Panel ────────────────────────────────────────────
        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createEmptyBorder(10, 10, 10, 10),
            BorderFactory.createTitledBorder("Licensed States")
        ));
        
        String[] columns = {"Location ID", "State"};
        licenseTable = new JTable(new Object[][]{}, columns);
        licenseTable.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        
        // Add row selection listener to update state combo
        licenseTable.getSelectionModel().addListSelectionListener(e -> {
            int selectedRow = licenseTable.getSelectedRow();
            if (selectedRow >= 0) {
                String stateName = (String) licenseTable.getValueAt(selectedRow, 1);
                if (stateName != null) {
                    stateCombo.setSelectedItem(stateName);
                }
            }
        });
        
        JScrollPane scrollPane = new JScrollPane(licenseTable);
        tablePanel.add(scrollPane, BorderLayout.CENTER);
        
        // ── Assembly ───────────────────────────────────────────────
        add(inputPanel, BorderLayout.NORTH);
        add(tablePanel, BorderLayout.CENTER);
    }

    private void loadPrincipals() {
        principalCombo.removeAllItems();
        List<Employee> principals = employeeDAO.getPrincipals();
        for (Employee principal : principals) {
            principalCombo.addItem(principal);
        }
    }

    private void loadStates() {
        stateCombo.removeAllItems();
        List<Location> locations = locationDAO.getAllLocations();
        for (Location loc : locations) {
            stateCombo.addItem(loc.getState());
        }
    }

    private void addLicense() {
        Employee selectedPrincipal = (Employee) principalCombo.getSelectedItem();
        String selectedState = (String) stateCombo.getSelectedItem();
        
        if (selectedPrincipal == null || selectedState == null) {
            JOptionPane.showMessageDialog(this, "Please select both a principal and a state.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        try {
            // Get location ID for the state
            Location location = null;
            List<Location> locations = locationDAO.getAllLocations();
            for (Location loc : locations) {
                if (loc.getState().equals(selectedState)) {
                    location = loc;
                    break;
                }
            }
            
            if (location == null) {
                JOptionPane.showMessageDialog(this, "Location not found.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            // Check if license already exists
            if (licensingExists(selectedPrincipal.getEmployeeId(), location.getLocationId())) {
                JOptionPane.showMessageDialog(this, "This principal is already licensed in " + selectedState + ".", "Duplicate License", JOptionPane.WARNING_MESSAGE);
                return;
            }
            
            // Insert into Principal_License table
            String insertSQL = "INSERT INTO Principal_License (employee_id, location_id) VALUES (?, ?)";
            try (Connection conn = DatabaseManager.getConnection();
                 PreparedStatement ps = conn.prepareStatement(insertSQL)) {
                ps.setInt(1, selectedPrincipal.getEmployeeId());
                ps.setInt(2, location.getLocationId());
                int rowsAffected = ps.executeUpdate();
                
                if (rowsAffected > 0) {
                    parent.setStatus("Principal licensed in " + selectedState + ".");
                    refreshLicenseTable();
                } else {
                    JOptionPane.showMessageDialog(this, "Failed to add license.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Database error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deleteLicense() {
        int selectedRow = licenseTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Please select a license to delete.", "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        int confirm = JOptionPane.showConfirmDialog(this, "Delete this license?", "Confirm", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;
        
        try {
            Employee selectedPrincipal = (Employee) principalCombo.getSelectedItem();
            int locationId = (int) licenseTable.getValueAt(selectedRow, 0);
            
            if (selectedPrincipal == null) {
                JOptionPane.showMessageDialog(this, "Please select a principal.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            // Delete from Principal_License table
            String deleteSQL = "DELETE FROM Principal_License WHERE employee_id = ? AND location_id = ?";
            try (Connection conn = DatabaseManager.getConnection();
                 PreparedStatement ps = conn.prepareStatement(deleteSQL)) {
                ps.setInt(1, selectedPrincipal.getEmployeeId());
                ps.setInt(2, locationId);
                int rowsAffected = ps.executeUpdate();
                
                if (rowsAffected > 0) {
                    parent.setStatus("License deleted.");
                    refreshLicenseTable();
                } else {
                    JOptionPane.showMessageDialog(this, "Failed to delete license.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Database error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void refreshLicenseTable() {
        try {
            Employee selectedPrincipal = (Employee) principalCombo.getSelectedItem();
            if (selectedPrincipal == null) {
                licenseTable.setModel(new javax.swing.table.DefaultTableModel(new Object[][]{}, 
                    new String[]{"Location ID", "State"}));
                return;
            }
            
            // Get licenses for selected principal
            String selectSQL = "SELECT l.location_id, l.state FROM Principal_License pl " +
                             "JOIN Location l ON pl.location_id = l.location_id " +
                             "WHERE pl.employee_id = ? " +
                             "ORDER BY l.state";
            
            try (Connection conn = DatabaseManager.getConnection();
                 PreparedStatement ps = conn.prepareStatement(selectSQL)) {
                ps.setInt(1, selectedPrincipal.getEmployeeId());
                ResultSet rs = ps.executeQuery();
                
                List<Object[]> rows = new ArrayList<>();
                while (rs.next()) {
                    rows.add(new Object[]{
                        rs.getInt("location_id"),
                        rs.getString("state")
                    });
                }
                
                Object[][] data = rows.toArray(new Object[0][]);
                licenseTable.setModel(new javax.swing.table.DefaultTableModel(data,
                    new String[]{"Location ID", "State"}));
                parent.setStatus("Licenses loaded for " + selectedPrincipal.getFullName());
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Database error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void refreshPrincipalLicenses() {
        loadPrincipals();
        refreshLicenseTable();
    }

    /** Public method for MainFrame to refresh principal dropdowns */
    public void refreshPrincipalDropdown() {
        loadPrincipals();
        refreshLicenseTable();
    }

    private boolean licensingExists(int employeeId, int locationId) {
        String checkSQL = "SELECT COUNT(*) FROM Principal_License WHERE employee_id = ? AND location_id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(checkSQL)) {
            ps.setInt(1, employeeId);
            ps.setInt(2, locationId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
}
