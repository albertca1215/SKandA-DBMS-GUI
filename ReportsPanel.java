package ska.ui;

import ska.db.*;
import ska.model.*;
import javax.swing.*;
import java.awt.*;
import java.util.List;

/**
 * Panel for managing employees in the SK&A Database.
 * Allows users to view, add, edit, and delete employees.
 */
public class EmployeePanel extends JPanel {

    private MainFrame parent;
    private EmployeeDAO employeeDAO;
    private RankDAO rankDAO;
    
    private JTable employeeTable;
    private JTextField firstNameField, lastNameField;
    private JComboBox<Rank> rankCombo;
    private JButton addBtn, updateBtn, deleteBtn, refreshBtn;

    public EmployeePanel(MainFrame parent) {
        this.parent = parent;
        this.employeeDAO = new EmployeeDAO();
        this.rankDAO = new RankDAO();
        
        setLayout(new BorderLayout());
        buildUI();
        refreshEmployeeList();
    }

    private void buildUI() {
        // ── Input Panel ────────────────────────────────────────────
        JPanel inputPanel = new JPanel(new GridBagLayout());
        inputPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createEmptyBorder(10, 10, 10, 10),
            BorderFactory.createTitledBorder("Add/Edit Employee")
        ));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        
        // First Name
        gbc.gridx = 0; gbc.gridy = 0;
        inputPanel.add(new JLabel("First Name:"), gbc);
        gbc.gridx = 1;
        firstNameField = new JTextField(15);
        inputPanel.add(firstNameField, gbc);
        
        // Last Name
        gbc.gridx = 0; gbc.gridy = 1;
        inputPanel.add(new JLabel("Last Name:"), gbc);
        gbc.gridx = 1;
        lastNameField = new JTextField(15);
        inputPanel.add(lastNameField, gbc);
        
        // Rank
        gbc.gridx = 0; gbc.gridy = 2;
        inputPanel.add(new JLabel("Rank:"), gbc);
        gbc.gridx = 1;
        rankCombo = new JComboBox<>();
        loadRanksIntoCombo();
        inputPanel.add(rankCombo, gbc);
        
        // Buttons
        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 2;
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        
        addBtn = new JButton("Add");
        addBtn.addActionListener(e -> addEmployee());
        buttonPanel.add(addBtn);
        
        updateBtn = new JButton("Update");
        updateBtn.addActionListener(e -> updateEmployee());
        buttonPanel.add(updateBtn);
        
        deleteBtn = new JButton("Delete");
        deleteBtn.addActionListener(e -> deleteEmployee());
        buttonPanel.add(deleteBtn);
        
        refreshBtn = new JButton("Refresh");
        refreshBtn.addActionListener(e -> refreshEmployeeList());
        buttonPanel.add(refreshBtn);
        
        inputPanel.add(buttonPanel, gbc);
        
        // ── Table Panel ────────────────────────────────────────────
        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createEmptyBorder(10, 10, 10, 10),
            BorderFactory.createTitledBorder("Employees")
        ));
        
        String[] columns = {"ID", "First Name", "Last Name", "Rank", "Cost Rate"};
        employeeTable = new JTable(new Object[][]{}, columns);
        employeeTable.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        
        // Add row selection listener to populate form fields
        employeeTable.getSelectionModel().addListSelectionListener(e -> {
            int selectedRow = employeeTable.getSelectedRow();
            if (selectedRow >= 0) {
                populateEmployeeFormFromRow(selectedRow);
            }
        });
        
        JScrollPane scrollPane = new JScrollPane(employeeTable);
        tablePanel.add(scrollPane, BorderLayout.CENTER);
        
        // ── Assembly ───────────────────────────────────────────────
        add(inputPanel, BorderLayout.NORTH);
        add(tablePanel, BorderLayout.CENTER);
    }

    private void loadRanksIntoCombo() {
        rankCombo.removeAllItems();
        List<Rank> ranks = rankDAO.getAllRanks();
        for (Rank rank : ranks) {
            rankCombo.addItem(rank);
        }
    }

    private void addEmployee() {
        try {
            String firstName = firstNameField.getText().trim();
            String lastName = lastNameField.getText().trim();
            Rank selectedRank = (Rank) rankCombo.getSelectedItem();
            
            if (firstName.isEmpty() || lastName.isEmpty() || selectedRank == null) {
                JOptionPane.showMessageDialog(this, "Please fill in all fields.", "Validation Error", JOptionPane.WARNING_MESSAGE);
                return;
            }
            
            Employee emp = new Employee(0, firstName, lastName, selectedRank.getRankId(), selectedRank.getRankName(), selectedRank.getCostRate());
            if (employeeDAO.addEmployee(emp)) {
                parent.setStatus("Employee added successfully.");
                clearFields();
                refreshEmployeeList();
                // Notify MainFrame to refresh dropdowns in other panels
                parent.refreshAllPanelDropdowns();
            } else {
                JOptionPane.showMessageDialog(this, "Failed to add employee.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateEmployee() {
        int selectedRow = employeeTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Please select an employee to update.", "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        try {
            int employeeId = (int) employeeTable.getValueAt(selectedRow, 0);
            String firstName = firstNameField.getText().trim();
            String lastName = lastNameField.getText().trim();
            Rank selectedRank = (Rank) rankCombo.getSelectedItem();
            
            if (firstName.isEmpty() || lastName.isEmpty() || selectedRank == null) {
                JOptionPane.showMessageDialog(this, "Please fill in all fields.", "Validation Error", JOptionPane.WARNING_MESSAGE);
                return;
            }
            
            Employee emp = new Employee(employeeId, firstName, lastName, selectedRank.getRankId(), selectedRank.getRankName(), selectedRank.getCostRate());
            if (employeeDAO.updateEmployee(emp)) {
                parent.setStatus("Employee updated successfully.");
                clearFields();
                refreshEmployeeList();
                // Notify MainFrame to refresh dropdowns in other panels
                parent.refreshAllPanelDropdowns();
            } else {
                JOptionPane.showMessageDialog(this, "Failed to update employee.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deleteEmployee() {
        int selectedRow = employeeTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Please select an employee to delete.", "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        int confirm = JOptionPane.showConfirmDialog(this, "Delete this employee?", "Confirm", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;
        
        try {
            int employeeId = (int) employeeTable.getValueAt(selectedRow, 0);
            if (employeeDAO.deleteEmployee(employeeId)) {
                parent.setStatus("Employee deleted successfully.");
                clearFields();
                refreshEmployeeList();
                // Notify MainFrame to refresh dropdowns in other panels
                parent.refreshAllPanelDropdowns();
            } else {
                JOptionPane.showMessageDialog(this, "Failed to delete employee.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void refreshEmployeeList() {
        try {
            List<Employee> employees = employeeDAO.getAllEmployees();
            Object[][] data = new Object[employees.size()][5];
            
            for (int i = 0; i < employees.size(); i++) {
                Employee emp = employees.get(i);
                data[i][0] = emp.getEmployeeId();
                data[i][1] = emp.getFirstName();
                data[i][2] = emp.getLastName();
                data[i][3] = emp.getRankName();
                data[i][4] = "$" + String.format("%.2f", emp.getCostRate());
            }
            
            employeeTable.setModel(new javax.swing.table.DefaultTableModel(data, 
                new String[]{"ID", "First Name", "Last Name", "Rank", "Cost Rate"}));
            parent.setStatus("Employees loaded.");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error loading employees: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /** Populates form fields with data from the selected table row */
    private void populateEmployeeFormFromRow(int rowIndex) {
        try {
            int employeeId = (int) employeeTable.getValueAt(rowIndex, 0);
            Employee employee = employeeDAO.getEmployeeById(employeeId);
            
            if (employee != null) {
                firstNameField.setText(employee.getFirstName());
                lastNameField.setText(employee.getLastName());
                
                // Set rank combo
                for (int i = 0; i < rankCombo.getItemCount(); i++) {
                    Rank rank = (Rank) rankCombo.getItemAt(i);
                    if (rank.getRankId() == employee.getRankId()) {
                        rankCombo.setSelectedIndex(i);
                        break;
                    }
                }
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error loading employee details: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void clearFields() {
        firstNameField.setText("");
        lastNameField.setText("");
        if (rankCombo.getItemCount() > 0) rankCombo.setSelectedIndex(0);
        employeeTable.clearSelection();
    }
}
