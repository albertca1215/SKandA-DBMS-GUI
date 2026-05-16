package ska.ui;

import ska.db.*;
import ska.model.*;
import javax.swing.*;
import java.awt.*;
import java.util.List;

/**
 * Panel for calculating project profitability.
 * Profitability = Fee - (Sum of Man-Hours × Cost Rate for all employees)
 */
public class ProfitabilityPanel extends JPanel {

    private MainFrame parent;
    private ProjectDAO projectDAO;
    private ManHoursDAO manHoursDAO;
    private EmployeeDAO employeeDAO;
    
    private JComboBox<Project> projectCombo;
    private JTextField feeField, totalCostField, profitabilityField;
    private JTable manHoursTable;
    private JButton calculateBtn;

    public ProfitabilityPanel(MainFrame parent) {
        this.parent = parent;
        this.projectDAO = new ProjectDAO();
        this.manHoursDAO = new ManHoursDAO();
        this.employeeDAO = new EmployeeDAO();
        
        setLayout(new BorderLayout());
        buildUI();
    }

    private void buildUI() {
        // ── Input Panel ────────────────────────────────────────────
        JPanel inputPanel = new JPanel(new GridBagLayout());
        inputPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createEmptyBorder(10, 10, 10, 10),
            BorderFactory.createTitledBorder("Calculate Profitability")
        ));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        
        // Project Selection
        gbc.gridx = 0; gbc.gridy = 0;
        inputPanel.add(new JLabel("Select Project:"), gbc);
        gbc.gridx = 1; gbc.gridwidth = 2;
        projectCombo = new JComboBox<>();
        loadProjects();
        projectCombo.addActionListener(e -> loadProjectDetails());
        inputPanel.add(projectCombo, gbc);
        
        // Fee Display
        gbc.gridx = 0; gbc.gridy = 1; gbc.gridwidth = 1;
        inputPanel.add(new JLabel("Contract Fee per Employee:"), gbc);
        gbc.gridx = 1;
        feeField = new JTextField(20);
        feeField.setEditable(false);
        inputPanel.add(feeField, gbc);
        
        // Total Cost
        gbc.gridx = 0; gbc.gridy = 2;
        inputPanel.add(new JLabel("Total Cost (Man-Hours × Rates):"), gbc);
        gbc.gridx = 1;
        totalCostField = new JTextField(20);
        totalCostField.setEditable(false);
        inputPanel.add(totalCostField, gbc);
        
        // Profitability
        gbc.gridx = 0; gbc.gridy = 3;
        inputPanel.add(new JLabel("Profitability (Fee - Cost):"), gbc);
        gbc.gridx = 1;
        profitabilityField = new JTextField(20);
        profitabilityField.setEditable(false);
        profitabilityField.setBackground(new Color(220, 255, 220));
        profitabilityField.setFont(profitabilityField.getFont().deriveFont(Font.BOLD));
        inputPanel.add(profitabilityField, gbc);
        
        // Calculate Button
        gbc.gridx = 0; gbc.gridy = 4; gbc.gridwidth = 2;
        calculateBtn = new JButton("Calculate Profitability");
        calculateBtn.addActionListener(e -> calculateProfitability());
        inputPanel.add(calculateBtn, gbc);
        
        // ── Man-Hours Table Panel ──────────────────────────────────
        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createEmptyBorder(10, 10, 10, 10),
            BorderFactory.createTitledBorder("Project Man-Hours by Employee")
        ));
        
        String[] columns = {"Employee", "Rank", "Man-Hours", "Cost/Hr", "Total Cost"};
        manHoursTable = new JTable(new Object[][]{}, columns);
        JScrollPane scrollPane = new JScrollPane(manHoursTable);
        tablePanel.add(scrollPane, BorderLayout.CENTER);
        
        // ── Assembly ───────────────────────────────────────────────
        add(inputPanel, BorderLayout.NORTH);
        add(tablePanel, BorderLayout.CENTER);
    }

    private void loadProjects() {
        projectCombo.removeAllItems();
        List<Project> projects = projectDAO.getAllProjects();
        for (Project p : projects) {
            projectCombo.addItem(p);
        }
    }

    private void loadProjectDetails() {
        Project selectedProject = (Project) projectCombo.getSelectedItem();
        if (selectedProject != null) {
            feeField.setText("$" + String.format("%.2f", selectedProject.getFee()));
        }
    }

    private void calculateProfitability() {
        Project selectedProject = (Project) projectCombo.getSelectedItem();
        if (selectedProject == null) {
            JOptionPane.showMessageDialog(this, "Please select a project.", "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        try {
            // Get all man-hours for this project
            List<ManHours> hours = manHoursDAO.getManHoursByProject(selectedProject.getProjectId());
            
            // Calculate total cost
            double totalCost = 0;
            Object[][] data = new Object[hours.size()][5];
            
            for (int i = 0; i < hours.size(); i++) {
                ManHours mh = hours.get(i);
                Employee emp = employeeDAO.getAllEmployees().stream()
                    .filter(e -> e.getEmployeeId() == mh.getEmployeeId())
                    .findFirst().orElse(null);
                
                if (emp != null) {
                    double manHoursCost = mh.getManHours() * emp.getCostRate();
                    totalCost += manHoursCost;
                    
                    data[i][0] = emp.getFullName();
                    data[i][1] = emp.getRankName();
                    data[i][2] = mh.getManHours();
                    data[i][3] = "$" + String.format("%.2f", emp.getCostRate());
                    data[i][4] = "$" + String.format("%.2f", manHoursCost);
                }
            }
            
            // Update table
            manHoursTable.setModel(new javax.swing.table.DefaultTableModel(data,
                new String[]{"Employee", "Rank", "Man-Hours", "Cost/Hr", "Total Cost"}));
            
            // Update cost and profitability fields
            totalCostField.setText("$" + String.format("%.2f", totalCost));
            double profitability = selectedProject.getFee() - totalCost;
            profitabilityField.setText("$" + String.format("%.2f", profitability));
            
            // Change color based on profitability
            if (profitability >= 0) {
                profitabilityField.setBackground(new Color(200, 255, 200));  // Green
            } else {
                profitabilityField.setBackground(new Color(255, 200, 200));  // Red
            }
            
            parent.setStatus("Profitability calculated.");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error calculating profitability: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /** Public method for MainFrame to refresh project dropdown */
    public void refreshProjectDropdown() {
        loadProjects();
    }
}
