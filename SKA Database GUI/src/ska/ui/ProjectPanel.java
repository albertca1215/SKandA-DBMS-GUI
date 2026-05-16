package ska.ui;

import ska.db.*;
import ska.model.*;
import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Panel for managing projects in the SK&A Database.
 * Allows users to view, add, edit, and delete projects.
 */
public class ProjectPanel extends JPanel {

    private MainFrame parent;
    private ProjectDAO projectDAO;
    private ArchitectDAO architectDAO;
    private OwnerDAO ownerDAO;
    private LocationDAO locationDAO;
    private EmployeeDAO employeeDAO;
    private ska.db.ManHoursDAO manHoursDAO;
    
    private JTable projectTable;
    private JTextField projectNameField, feeField, architectField, ownerField;
    private JComboBox<String> sectorCombo, materialCombo, locationCombo;
    private JComboBox<Employee> principalCombo, pmCombo;
    private JButton addBtn, updateBtn, deleteBtn, refreshBtn;
    
    // Map to store Location -> locationId for easy lookup during add/update
    private Map<String, Integer> locationMap = new HashMap<>();

    public ProjectPanel(MainFrame parent) {
        this.parent = parent;
        this.projectDAO = new ProjectDAO();
        this.architectDAO = new ArchitectDAO();
        this.ownerDAO = new OwnerDAO();
        this.locationDAO = new LocationDAO();
        this.employeeDAO = new EmployeeDAO();
        this.manHoursDAO = new ska.db.ManHoursDAO();
        
        setLayout(new BorderLayout());
        buildUI();
        refreshProjectList();
    }

    private void buildUI() {
        // ── Input Panel ────────────────────────────────────────────
        JPanel inputPanel = new JPanel(new GridBagLayout());
        inputPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createEmptyBorder(10, 10, 10, 10),
            BorderFactory.createTitledBorder("Add/Edit Project")
        ));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(3, 3, 3, 3);
        gbc.anchor = GridBagConstraints.WEST;
        
        // Project Name
        gbc.gridx = 0; gbc.gridy = 0;
        inputPanel.add(new JLabel("Project Name:"), gbc);
        gbc.gridx = 1;
        projectNameField = new JTextField(15);
        inputPanel.add(projectNameField, gbc);
        
        // Sector
        gbc.gridx = 2; gbc.gridy = 0;
        inputPanel.add(new JLabel("Sector:"), gbc);
        gbc.gridx = 3;
        sectorCombo = new JComboBox<>(new String[]{"Residential", "Office", "Mixed Use", "Repair", "Renovation"});
        inputPanel.add(sectorCombo, gbc);
        
        // Material Type
        gbc.gridx = 0; gbc.gridy = 1;
        inputPanel.add(new JLabel("Material:"), gbc);
        gbc.gridx = 1;
        materialCombo = new JComboBox<>(new String[]{"Concrete", "Steel", "Wood"});
        inputPanel.add(materialCombo, gbc);
        
        // Location (State)
        gbc.gridx = 2; gbc.gridy = 1;
        inputPanel.add(new JLabel("Location:"), gbc);
        gbc.gridx = 3;
        locationCombo = new JComboBox<>();
        loadLocations();
        inputPanel.add(locationCombo, gbc);
        
        // Fee
        gbc.gridx = 0; gbc.gridy = 2;
        inputPanel.add(new JLabel("Fee (per Employee):"), gbc);
        gbc.gridx = 1;
        feeField = new JTextField(15);
        inputPanel.add(feeField, gbc);
        
        // Owner
        gbc.gridx = 2; gbc.gridy = 2;
        inputPanel.add(new JLabel("Owner:"), gbc);
        gbc.gridx = 3;
        ownerField = new JTextField(15);
        inputPanel.add(ownerField, gbc);
        
        // Architect
        gbc.gridx = 0; gbc.gridy = 3;
        inputPanel.add(new JLabel("Architect:"), gbc);
        gbc.gridx = 1;
        architectField = new JTextField(15);
        inputPanel.add(architectField, gbc);
        
        // Principal
        gbc.gridx = 2; gbc.gridy = 3;
        inputPanel.add(new JLabel("Principal:"), gbc);
        gbc.gridx = 3;
        principalCombo = new JComboBox<>();
        inputPanel.add(principalCombo, gbc);
        
        // Project Manager
        gbc.gridx = 0; gbc.gridy = 4;
        inputPanel.add(new JLabel("Project Manager:"), gbc);
        gbc.gridx = 1;
        pmCombo = new JComboBox<>();
        inputPanel.add(pmCombo, gbc);
        
        // Load data after all comboboxes are created
        loadLocations();
        loadEmployees();
        
        // Buttons
        gbc.gridx = 0; gbc.gridy = 5; gbc.gridwidth = 4;
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        
        addBtn = new JButton("Add");
        addBtn.addActionListener(e -> addProject());
        buttonPanel.add(addBtn);
        
        updateBtn = new JButton("Update");
        updateBtn.addActionListener(e -> updateProject());
        buttonPanel.add(updateBtn);
        
        deleteBtn = new JButton("Delete");
        deleteBtn.addActionListener(e -> deleteProject());
        buttonPanel.add(deleteBtn);
        
        refreshBtn = new JButton("Refresh");
        refreshBtn.addActionListener(e -> refreshProjectList());
        buttonPanel.add(refreshBtn);
        
        inputPanel.add(buttonPanel, gbc);
        
        // ── Table Panel ────────────────────────────────────────────
        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createEmptyBorder(10, 10, 10, 10),
            BorderFactory.createTitledBorder("Projects")
        ));
        
        String[] columns = {"ID", "Name", "Principal", "PM", "Sector", "Material", "Owner", "Architect", "Location", "Fee"};
        projectTable = new JTable(new Object[][]{}, columns);
        projectTable.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        
        // Add row selection listener to populate form fields
        projectTable.getSelectionModel().addListSelectionListener(e -> {
            int selectedRow = projectTable.getSelectedRow();
            if (selectedRow >= 0) {
                populateProjectFormFromRow(selectedRow);
            }
        });
        
        JScrollPane scrollPane = new JScrollPane(projectTable);
        tablePanel.add(scrollPane, BorderLayout.CENTER);
        
        // ── Assembly ───────────────────────────────────────────────
        JScrollPane inputScroll = new JScrollPane(inputPanel);
        add(inputScroll, BorderLayout.NORTH);
        add(tablePanel, BorderLayout.CENTER);
    }

    private void loadLocations() {
        locationCombo.removeAllItems();
        locationMap.clear();
        List<Location> locations = locationDAO.getAllLocations();
        for (Location loc : locations) {
            locationCombo.addItem(loc.getState());
            locationMap.put(loc.getState(), loc.getLocationId());
        }
    }

    private void loadEmployees() {
        principalCombo.removeAllItems();
        pmCombo.removeAllItems();
        
        // Load only principals for Principal dropdown
        List<Employee> principals = employeeDAO.getPrincipals();
        for (Employee principal : principals) {
            principalCombo.addItem(principal);
        }
        
        // Load only project managers for PM dropdown
        List<Employee> projectManagers = employeeDAO.getProjectManagers();
        for (Employee pm : projectManagers) {
            pmCombo.addItem(pm);
        }
    }

    private void addProject() {
        try {
            String name = projectNameField.getText().trim();
            String sector = (String) sectorCombo.getSelectedItem();
            String material = (String) materialCombo.getSelectedItem();
            String locationName = (String) locationCombo.getSelectedItem();
            int locationId = locationMap.getOrDefault(locationName, 0);
            double fee = Double.parseDouble(feeField.getText().trim());
            
            String architectName = architectField.getText().trim();
            String ownerName = ownerField.getText().trim();
            Employee principal = (Employee) principalCombo.getSelectedItem();
            Employee pm = (Employee) pmCombo.getSelectedItem();
            
            if (name.isEmpty() || ownerName.isEmpty() || architectName.isEmpty() || principal == null || pm == null || locationId == 0) {
                JOptionPane.showMessageDialog(this, "Please fill in all fields.", "Validation Error", JOptionPane.WARNING_MESSAGE);
                return;
            }
            
            // Verify that the selected Principal is licensed in the chosen state
            if (!isPrincipalLicensedInState(principal.getEmployeeId(), locationId)) {
                JOptionPane.showMessageDialog(this, "The selected Principal is not licensed in " + locationName + ". " +
                    "Please license them in that state via the Principal Licenses tab.", 
                    "Licensing Error", JOptionPane.WARNING_MESSAGE);
                return;
            }
            
            // Get or create Architect
            Architect architect = architectDAO.getArchitectByName(architectName);
            if (architect == null) {
                architect = new Architect(0, architectName);
                if (!architectDAO.addArchitect(architect)) {
                    JOptionPane.showMessageDialog(this, "Failed to create architect.", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                architect = architectDAO.getArchitectByName(architectName);
            }
            
            // Get or create Owner
            Owner owner = ownerDAO.getOwnerByName(ownerName);
            if (owner == null) {
                owner = new Owner(0, ownerName);
                if (!ownerDAO.addOwner(owner)) {
                    JOptionPane.showMessageDialog(this, "Failed to create owner.", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                owner = ownerDAO.getOwnerByName(ownerName);
            }
            
            Project proj = new Project(0, name, sector, material, fee, locationId, locationName,
                owner.getOwnerId(), architect.getArchitectId(),
                principal.getEmployeeId(), pm.getEmployeeId());
            
            int newId = projectDAO.addProjectReturnId(proj);
            if (newId > 0) {
                parent.setStatus("Project added successfully.");
                // Ensure default PM and Principal assignments
                ensureDefaultAssignments(newId, principal.getEmployeeId(), pm.getEmployeeId());
                clearFields();
                refreshProjectList();
                parent.refreshAllPanelDropdowns(); // Trigger dropdown refresh in all panels
                parent.refreshManHoursPanel();
            } else {
                JOptionPane.showMessageDialog(this, "Failed to add project.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Please enter a valid fee amount.", "Validation Error", JOptionPane.WARNING_MESSAGE);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateProject() {
        int selectedRow = projectTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Please select a project to update.", "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        try {
            int projectId = (int) projectTable.getValueAt(selectedRow, 0);
            Project existingProject = projectDAO.getProjectById(projectId);
            String name = projectNameField.getText().trim();
            String sector = (String) sectorCombo.getSelectedItem();
            String material = (String) materialCombo.getSelectedItem();
            String locationName = (String) locationCombo.getSelectedItem();
            int locationId = locationMap.getOrDefault(locationName, 0);
            double fee = Double.parseDouble(feeField.getText().trim());
            
            String architectName = architectField.getText().trim();
            String ownerName = ownerField.getText().trim();
            Employee principal = (Employee) principalCombo.getSelectedItem();
            Employee pm = (Employee) pmCombo.getSelectedItem();
            
            if (name.isEmpty() || ownerName.isEmpty() || architectName.isEmpty() || principal == null || pm == null || locationId == 0) {
                JOptionPane.showMessageDialog(this, "Please fill in all fields.", "Validation Error", JOptionPane.WARNING_MESSAGE);
                return;
            }
            
            // Verify that the selected Principal is licensed in the chosen state
            if (!isPrincipalLicensedInState(principal.getEmployeeId(), locationId)) {
                JOptionPane.showMessageDialog(this, "The selected Principal is not licensed in " + locationName + ". " +
                    "Please license them in that state via the Principal Licenses tab.", 
                    "Licensing Error", JOptionPane.WARNING_MESSAGE);
                return;
            }
            
            // Get or create Architect
            Architect architect = architectDAO.getArchitectByName(architectName);
            if (architect == null) {
                architect = new Architect(0, architectName);
                if (!architectDAO.addArchitect(architect)) {
                    JOptionPane.showMessageDialog(this, "Failed to create architect.", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                architect = architectDAO.getArchitectByName(architectName);
            }
            
            // Get or create Owner
            Owner owner = ownerDAO.getOwnerByName(ownerName);
            if (owner == null) {
                owner = new Owner(0, ownerName);
                if (!ownerDAO.addOwner(owner)) {
                    JOptionPane.showMessageDialog(this, "Failed to create owner.", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                owner = ownerDAO.getOwnerByName(ownerName);
            }
            
            Project proj = new Project(projectId, name, sector, material, fee, locationId, locationName,
                owner.getOwnerId(), architect.getArchitectId(),
                principal.getEmployeeId(), pm.getEmployeeId());
            
            if (projectDAO.updateProject(proj)) {
                parent.setStatus("Project updated successfully.");
                syncDefaultAssignments(
                    projectId,
                    existingProject != null ? existingProject.getPrincipalId() : 0,
                    principal.getEmployeeId(),
                    existingProject != null ? existingProject.getProjectManagerId() : 0,
                    pm.getEmployeeId()
                );
                // Ensure default assignments exist (do not duplicate)
                ensureDefaultAssignments(projectId, principal.getEmployeeId(), pm.getEmployeeId());
                clearFields();
                refreshProjectList();
                parent.refreshAllPanelDropdowns(); // Trigger dropdown refresh in all panels
                parent.refreshManHoursPanel();
            } else {
                JOptionPane.showMessageDialog(this, "Failed to update project.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Please enter a valid fee amount.", "Validation Error", JOptionPane.WARNING_MESSAGE);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deleteProject() {
        int selectedRow = projectTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Please select a project to delete.", "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        int confirm = JOptionPane.showConfirmDialog(this, "Delete this project?", "Confirm", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;
        
        try {
            int projectId = (int) projectTable.getValueAt(selectedRow, 0);
            if (projectDAO.deleteProject(projectId)) {
                parent.setStatus("Project deleted successfully.");
                clearFields();
                refreshProjectList();
                parent.refreshAllPanelDropdowns(); // Trigger dropdown refresh in all panels
            } else {
                JOptionPane.showMessageDialog(this, "Failed to delete project.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /** Ensure PM and Principal are assigned to the project with default hours if not already assigned. */
    private void ensureDefaultAssignments(int projectId, int principalId, int pmId) {
        try {
            // PM default: 7 hours, role = PM
            if (pmId > 0 && !manHoursDAO.assignmentExists(projectId, pmId)) {
                manHoursDAO.addManHours(new ska.model.ManHours(0, projectId, pmId, 7.0, "PM"));
            }
            // Principal default: 12 hours, role = Principal
            if (principalId > 0 && !manHoursDAO.assignmentExists(projectId, principalId)) {
                manHoursDAO.addManHours(new ska.model.ManHours(0, projectId, principalId, 12.0, "Principal"));
            }
        } catch (Exception e) {
            // Log and continue
            e.printStackTrace();
        }
    }

    /** Sync default PM/Principal assignments when a project's PM or Principal changes. */
    private void syncDefaultAssignments(int projectId, int oldPrincipalId, int newPrincipalId, int oldPmId, int newPmId) {
        try {
            // If PM changed, remove old PM-role assignment
            if (oldPmId > 0 && oldPmId != newPmId) {
                manHoursDAO.deleteEmployeeRoleFromProject(projectId, oldPmId, "PM");
            }
            // If Principal changed, remove old Principal-role assignment
            if (oldPrincipalId > 0 && oldPrincipalId != newPrincipalId) {
                manHoursDAO.deleteEmployeeRoleFromProject(projectId, oldPrincipalId, "Principal");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void refreshProjectList() {
        try {
            List<Project> projects = projectDAO.getAllProjects();
            List<Employee> employees = employeeDAO.getAllEmployees();
            List<Owner> owners = ownerDAO.getAllOwners();
            List<Architect> architects = architectDAO.getAllArchitects();
            
            // Build maps for quick lookups
            Map<Integer, String> employeeNameMap = new HashMap<>();
            for (Employee emp : employees) {
                employeeNameMap.put(emp.getEmployeeId(), emp.getFullName());
            }
            
            Map<Integer, String> ownerNameMap = new HashMap<>();
            for (Owner owner : owners) {
                ownerNameMap.put(owner.getOwnerId(), owner.getName());
            }
            
            Map<Integer, String> architectNameMap = new HashMap<>();
            for (Architect arch : architects) {
                architectNameMap.put(arch.getArchitectId(), arch.getName());
            }
            
            Object[][] data = new Object[projects.size()][10];
            
            for (int i = 0; i < projects.size(); i++) {
                Project p = projects.get(i);
                String principalName = employeeNameMap.getOrDefault(p.getPrincipalId(), "Unknown");
                String pmName = employeeNameMap.getOrDefault(p.getProjectManagerId(), "Unknown");
                String ownerName = ownerNameMap.getOrDefault(p.getOwnerId(), "Unknown");
                String architectName = architectNameMap.getOrDefault(p.getArchitectId(), "Unknown");
                
                data[i][0] = p.getProjectId();
                data[i][1] = p.getProjectName();
                data[i][2] = principalName;
                data[i][3] = pmName;
                data[i][4] = p.getSector();
                data[i][5] = p.getMaterial();
                data[i][6] = ownerName;
                data[i][7] = architectName;
                data[i][8] = p.getStateName();
                data[i][9] = "$" + String.format("%.2f", p.getFee());
            }
            
            projectTable.setModel(new javax.swing.table.DefaultTableModel(data,
                new String[]{"ID", "Name", "Principal", "PM", "Sector", "Material", "Owner", "Architect", "Location", "Fee"}));
            parent.setStatus("Projects loaded.");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error loading projects: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void clearFields() {
        projectNameField.setText("");
        feeField.setText("");
        architectField.setText("");
        ownerField.setText("");
        if (sectorCombo.getItemCount() > 0) sectorCombo.setSelectedIndex(0);
        if (materialCombo.getItemCount() > 0) materialCombo.setSelectedIndex(0);
        projectTable.clearSelection();
    }

    /** Populates form fields with data from the selected table row */
    private void populateProjectFormFromRow(int rowIndex) {
        try {
            int projectId = (int) projectTable.getValueAt(rowIndex, 0);
            Project project = projectDAO.getProjectById(projectId);
            
            if (project != null) {
                projectNameField.setText(project.getProjectName());
                feeField.setText(String.valueOf(project.getFee()));
                
                // Set sector combo
                if (sectorCombo.getItemCount() > 0) {
                    sectorCombo.setSelectedItem(project.getSector());
                }
                
                // Set material combo
                if (materialCombo.getItemCount() > 0) {
                    materialCombo.setSelectedItem(project.getMaterial());
                }
                
                // Set location combo
                if (locationCombo.getItemCount() > 0) {
                    locationCombo.setSelectedItem(project.getStateName());
                }
                
                // Set architect and owner fields
                Architect architect = architectDAO.getArchitectById(project.getArchitectId());
                if (architect != null) {
                    architectField.setText(architect.getName());
                }
                
                Owner owner = ownerDAO.getOwnerById(project.getOwnerId());
                if (owner != null) {
                    ownerField.setText(owner.getName());
                }
                
                // Set principal dropdown
                for (int i = 0; i < principalCombo.getItemCount(); i++) {
                    Employee emp = (Employee) principalCombo.getItemAt(i);
                    if (emp.getEmployeeId() == project.getPrincipalId()) {
                        principalCombo.setSelectedIndex(i);
                        break;
                    }
                }
                
                // Set PM dropdown
                for (int i = 0; i < pmCombo.getItemCount(); i++) {
                    Employee emp = (Employee) pmCombo.getItemAt(i);
                    if (emp.getEmployeeId() == project.getProjectManagerId()) {
                        pmCombo.setSelectedIndex(i);
                        break;
                    }
                }
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error loading project details: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /** Public method for MainFrame to refresh employee dropdowns */
    public void refreshEmployeeDropdowns() {
        loadEmployees();
    }
    
    /** Helper method to verify if a Principal is licensed in a specific state */
    private boolean isPrincipalLicensedInState(int employeeId, int locationId) {
        try {
            String checkSQL = "SELECT COUNT(*) FROM Principal_License WHERE employee_id = ? AND location_id = ?";
            try (java.sql.Connection conn = DatabaseManager.getConnection();
                 java.sql.PreparedStatement ps = conn.prepareStatement(checkSQL)) {
                ps.setInt(1, employeeId);
                ps.setInt(2, locationId);
                java.sql.ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (java.sql.SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
}
