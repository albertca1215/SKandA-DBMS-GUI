package ska.ui;

import ska.db.*;
import ska.model.*;
import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Panel for managing Project_Employee assignments and man-hours.
 * Allows users to assign employees to projects and record hours worked.
 */
public class ManHoursPanel extends JPanel {

    private MainFrame parent;
    private ProjectDAO projectDAO;
    private EmployeeDAO employeeDAO;
    private ManHoursDAO manHoursDAO;
    
    private JTable manHoursTable;
    private JComboBox<Project> projectCombo;
    private JComboBox<Employee> employeeCombo;
    private JTextField manHoursField;
    private JComboBox<String> roleCombo;
    private JButton addBtn, updateBtn, deleteBtn, refreshBtn;
    
    private Map<String, Integer> projectMap = new HashMap<>();
    private Map<String, Integer> employeeMap = new HashMap<>();

    public ManHoursPanel(MainFrame parent) {
        this.parent = parent;
        this.projectDAO = new ProjectDAO();
        this.employeeDAO = new EmployeeDAO();
        this.manHoursDAO = new ManHoursDAO();
        
        setLayout(new BorderLayout());
        buildUI();
        refreshManHoursList();
    }

    private void buildUI() {
        // ── Input Panel ────────────────────────────────────────────
        JPanel inputPanel = new JPanel(new GridBagLayout());
        inputPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createEmptyBorder(10, 10, 10, 10),
            BorderFactory.createTitledBorder("Assign Employee to Project")
        ));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        
        // Project
        gbc.gridx = 0; gbc.gridy = 0;
        inputPanel.add(new JLabel("Project:"), gbc);
        gbc.gridx = 1;
        projectCombo = new JComboBox<>();
        loadProjects();
        inputPanel.add(projectCombo, gbc);
        
        // Employee
        gbc.gridx = 0; gbc.gridy = 1;
        inputPanel.add(new JLabel("Employee:"), gbc);
        gbc.gridx = 1;
        employeeCombo = new JComboBox<>();
        loadEmployees();
        employeeCombo.addActionListener(e -> updateRoleDropdown());
        inputPanel.add(employeeCombo, gbc);
        
        // Man-Hours
        gbc.gridx = 0; gbc.gridy = 2;
        inputPanel.add(new JLabel("Man-Hours:"), gbc);
        gbc.gridx = 1;
        manHoursField = new JTextField(15);
        inputPanel.add(manHoursField, gbc);
        
        // Role on Project
        gbc.gridx = 0; gbc.gridy = 3;
        inputPanel.add(new JLabel("Role:"), gbc);
        gbc.gridx = 1;
        roleCombo = new JComboBox<>();
        inputPanel.add(roleCombo, gbc);
        
        // Buttons
        gbc.gridx = 0; gbc.gridy = 4; gbc.gridwidth = 2;
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        
        addBtn = new JButton("Add");
        addBtn.addActionListener(e -> addManHours());
        buttonPanel.add(addBtn);
        
        updateBtn = new JButton("Update");
        updateBtn.addActionListener(e -> updateManHours());
        buttonPanel.add(updateBtn);
        
        deleteBtn = new JButton("Delete");
        deleteBtn.addActionListener(e -> deleteManHours());
        buttonPanel.add(deleteBtn);
        
        refreshBtn = new JButton("Refresh");
        refreshBtn.addActionListener(e -> refreshManHoursList());
        buttonPanel.add(refreshBtn);
        
        inputPanel.add(buttonPanel, gbc);
        
        // ── Table Panel ────────────────────────────────────────────
        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createEmptyBorder(10, 10, 10, 10),
            BorderFactory.createTitledBorder("Project Assignments")
        ));
        
        // Table model will include an internal Record ID (project_employee_id) in column 0
        // which we hide from the user. Visible columns start at index 1.
        String[] columns = {"Record ID", "Project ID", "Project Name", "Employee ID", "Employee Name", "Rank", "Man-Hours", "Role"};
        manHoursTable = new JTable(new Object[][]{}, columns);
        manHoursTable.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        
        // Add row selection listener to populate form fields
        manHoursTable.getSelectionModel().addListSelectionListener(e -> {
            int selectedRow = manHoursTable.getSelectedRow();
            if (selectedRow >= 0) {
                populateManHoursFormFromRow(selectedRow);
            }
        });
        
        JScrollPane scrollPane = new JScrollPane(manHoursTable);
        tablePanel.add(scrollPane, BorderLayout.CENTER);
        
        add(inputPanel, BorderLayout.NORTH);
        add(tablePanel, BorderLayout.CENTER);
    }

    private void loadProjects() {
        projectCombo.removeAllItems();
        List<Project> projects = projectDAO.getAllProjects();
        projectMap.clear();
        for (Project p : projects) {
            projectCombo.addItem(p);
            projectMap.put(p.getProjectName(), p.getProjectId());
        }
    }

    private void loadEmployees() {
        employeeCombo.removeAllItems();
        List<Employee> employees = employeeDAO.getAllEmployees();
        employeeMap.clear();
        for (Employee emp : employees) {
            employeeCombo.addItem(emp);
            employeeMap.put(emp.getFullName(), emp.getEmployeeId());
        }
    }

    private void addManHours() {
        try {
            Project selectedProject = (Project) projectCombo.getSelectedItem();
            Employee selectedEmployee = (Employee) employeeCombo.getSelectedItem();
            String hoursText = manHoursField.getText().trim();
            String role = (String) roleCombo.getSelectedItem();
            
            if (selectedProject == null || selectedEmployee == null || hoursText.isEmpty() || role == null) {
                JOptionPane.showMessageDialog(this, "Please fill in Project, Employee, Man-Hours, and Role.", "Validation Error", JOptionPane.WARNING_MESSAGE);
                return;
            }
            
            double hours = Double.parseDouble(hoursText);
            ManHours mh = new ManHours(0, selectedProject.getProjectId(), selectedEmployee.getEmployeeId(), hours, role);
            
            if (manHoursDAO.addManHours(mh)) {
                parent.setStatus("Man-hours assignment added successfully.");
                clearFields();
                refreshManHoursList();
                loadProjects(); // Refresh project dropdown
                loadEmployees(); // Refresh employee dropdown
            } else {
                JOptionPane.showMessageDialog(this, "Failed to add assignment.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Man-Hours must be a valid number.", "Validation Error", JOptionPane.WARNING_MESSAGE);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateManHours() {
        int selectedRow = manHoursTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Please select a record to update.", "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        try {
            int projectEmployeeId = (int) manHoursTable.getValueAt(selectedRow, 0);
            String hoursText = manHoursField.getText().trim();
            String role = (String) roleCombo.getSelectedItem();
            Employee selectedEmployee = (Employee) employeeCombo.getSelectedItem();
            Project selectedProject = (Project) projectCombo.getSelectedItem();
            
            if (selectedEmployee == null || hoursText.isEmpty() || role == null) {
                JOptionPane.showMessageDialog(this, "Please fill in Employee, Man-Hours, and Role.", "Validation Error", JOptionPane.WARNING_MESSAGE);
                return;
            }
            
            double hours = Double.parseDouble(hoursText);
            int projectId = selectedProject != null ? selectedProject.getProjectId() : (int) manHoursTable.getValueAt(selectedRow, 1);
            ManHours mh = new ManHours(projectEmployeeId, projectId, selectedEmployee.getEmployeeId(), hours, role);
            
            if (manHoursDAO.updateManHours(mh)) {
                parent.setStatus("Assignment updated successfully.");
                clearFields();
                refreshManHoursList();
                loadProjects(); // Refresh project dropdown
                loadEmployees(); // Refresh employee dropdown
            } else {
                JOptionPane.showMessageDialog(this, "Failed to update assignment.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Man-Hours must be a valid number.", "Validation Error", JOptionPane.WARNING_MESSAGE);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deleteManHours() {
        int selectedRow = manHoursTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Please select a record to delete.", "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        int confirm = JOptionPane.showConfirmDialog(this, "Delete this assignment?", "Confirm", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;
        
        try {
            int projectEmployeeId = (int) manHoursTable.getValueAt(selectedRow, 0);
            if (manHoursDAO.deleteManHours(projectEmployeeId)) {
                parent.setStatus("Assignment deleted successfully.");
                clearFields();
                refreshManHoursList();
                loadProjects(); // Refresh project dropdown
                loadEmployees(); // Refresh employee dropdown
            } else {
                JOptionPane.showMessageDialog(this, "Failed to delete assignment.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void refreshManHoursList() {
        try {
            List<ManHours> hours = manHoursDAO.getAllManHours();
            Object[][] data = new Object[hours.size()][8];

            for (int i = 0; i < hours.size(); i++) {
                ManHours mh = hours.get(i);
                Project proj = projectDAO.getProjectById(mh.getProjectId());
                Employee emp = employeeDAO.getEmployeeById(mh.getEmployeeId());

                if (proj != null && emp != null) {
                    // Column 0: internal record id (project_employee_id) — hidden
                    data[i][0] = mh.getProjectEmployeeId();
                    // Column 1: project id (visible)
                    data[i][1] = mh.getProjectId();
                    data[i][2] = proj.getProjectName();
                    data[i][3] = mh.getEmployeeId();
                    data[i][4] = emp.getFullName();
                    data[i][5] = emp.getRankName();
                    data[i][6] = mh.getManHours();
                    data[i][7] = mh.getRoleOnProject();
                }
            }

            manHoursTable.setModel(new javax.swing.table.DefaultTableModel(data,
                new String[]{"Record ID", "Project ID", "Project Name", "Employee ID", "Employee Name", "Rank", "Man-Hours", "Role"}));

            // Hide the internal Record ID column from the user
            if (manHoursTable.getColumnModel().getColumnCount() > 0) {
                javax.swing.table.TableColumn col = manHoursTable.getColumnModel().getColumn(0);
                col.setMinWidth(0);
                col.setMaxWidth(0);
                col.setPreferredWidth(0);
                col.setResizable(false);
            }
            parent.setStatus("Man-hours loaded.");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error loading assignments: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /** Populates form fields with data from the selected table row */
    private void populateManHoursFormFromRow(int rowIndex) {
        try {
            int projectId = (int) manHoursTable.getValueAt(rowIndex, 1);
            int employeeId = (int) manHoursTable.getValueAt(rowIndex, 3);
            double manHours = Double.parseDouble(manHoursTable.getValueAt(rowIndex, 6).toString());
            String role = (String) manHoursTable.getValueAt(rowIndex, 7);
            
            // Set project combo
            Project selectedProject = projectDAO.getProjectById(projectId);
            if (selectedProject != null) {
                for (int i = 0; i < projectCombo.getItemCount(); i++) {
                    Project p = (Project) projectCombo.getItemAt(i);
                    if (p.getProjectId() == projectId) {
                        projectCombo.setSelectedIndex(i);
                        break;
                    }
                }
            }
            
            // Set employee combo
            Employee selectedEmployee = employeeDAO.getEmployeeById(employeeId);
            if (selectedEmployee != null) {
                for (int i = 0; i < employeeCombo.getItemCount(); i++) {
                    Employee e = (Employee) employeeCombo.getItemAt(i);
                    if (e.getEmployeeId() == employeeId) {
                        employeeCombo.setSelectedIndex(i);
                        break;
                    }
                }
                // Update role dropdown based on the selected employee
                updateRoleDropdown();
                // Set role selection
                if (role != null) {
                    for (int i = 0; i < roleCombo.getItemCount(); i++) {
                        if (roleCombo.getItemAt(i).equals(role)) {
                            roleCombo.setSelectedIndex(i);
                            break;
                        }
                    }
                }
            }
            
            // Set man-hours field
            manHoursField.setText(String.valueOf(manHours));
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error loading assignment details: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void clearFields() {
        if (projectCombo.getItemCount() > 0) projectCombo.setSelectedIndex(0);
        if (employeeCombo.getItemCount() > 0) employeeCombo.setSelectedIndex(0);
        manHoursField.setText("");
        updateRoleDropdown();
        manHoursTable.clearSelection();
    }

    /** Public method for MainFrame to refresh project and employee dropdowns */
    public void refreshProjectAndEmployeeDropdowns() {
        loadProjects();
        loadEmployees();
    }

    /** Update the role dropdown based on the selected employee's rank */
    private void updateRoleDropdown() {
        roleCombo.removeAllItems();
        Employee selectedEmployee = (Employee) employeeCombo.getSelectedItem();
        
        if (selectedEmployee != null) {
            String rankName = selectedEmployee.getRankName();
            String[] allowedRoles = getAllowedRoles(rankName);
            for (String role : allowedRoles) {
                roleCombo.addItem(role);
            }
        }
    }

    /** Get allowed roles based on employee rank */
    private String[] getAllowedRoles(String rankName) {
        if (rankName.contains("Project Engineer")) {
            return new String[]{"Engineer"};
        } else if (rankName.equals("Assistant Project Manager")) {
            return new String[]{"Engineer", "PM"};
        } else if (rankName.equals("Project Manager")) {
            return new String[]{"PM"};
        } else if (rankName.equals("Associate")) {
            return new String[]{"PM"};
        } else if (rankName.equals("Principal")) {
            return new String[]{"Principal"};
        }
        return new String[]{"Other"};
    }
}
