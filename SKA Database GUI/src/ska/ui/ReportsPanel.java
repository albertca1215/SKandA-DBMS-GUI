package ska.ui;

import ska.db.*;
import ska.model.*;
import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Panel for generating various reports from the SK&A Database.
 * Includes employee directory, projects by type, principals by state, etc.
 */
public class ReportsPanel extends JPanel {

    private MainFrame parent;
    private EmployeeDAO employeeDAO;
    private ProjectDAO projectDAO;
    private LocationDAO locationDAO;
    private ArchitectDAO architectDAO;
    private OwnerDAO ownerDAO;
    
    private JTabbedPane reportTabs;
    private JTable employeeDirectoryTable;
    private JTable projectsTable;
    private JTable principalsTable;
    private JTable workHistoryTable;
    private JTable contractorTable;
    // New tables for additional reports
    private JTable principalProfitabilityTable;
    private JTable materialProfitabilityTable;
    private JTable architectProfitabilityTable;
    private JTable ownerProfitabilityTable;
    private JTable profitablePMTable;
    private JTable employeesForOwnerTable;
    private JTable architectMultipleTable;
    private JTable engineersForPMTable;
    private JTable projectLocationTable;
    
    // Dropdown references for runtime refresh
    private JComboBox<String> employeesForOwnerCombo;
    private JComboBox<Employee> engineersForPMCombo;
    private JComboBox<String> projectLocationOwnerCombo;
    private JComboBox<Employee> projectLocationPMCombo;

    public ReportsPanel(MainFrame parent) {
        this.parent = parent;
        this.employeeDAO = new EmployeeDAO();
        this.projectDAO = new ProjectDAO();
        this.locationDAO = new LocationDAO();
        this.architectDAO = new ArchitectDAO();
        this.ownerDAO = new OwnerDAO();
        
        setLayout(new BorderLayout());
        buildUI();
    }

    private void buildUI() {
        reportTabs = new JTabbedPane();
        reportTabs.setTabPlacement(JTabbedPane.TOP);
        
        // Tab 1: Employee Directory
        reportTabs.addTab("Employee Directory", buildEmployeeDirectoryTab());
        
        // Tab 2: Projects by Type
        reportTabs.addTab("Projects by Type/Location", buildProjectsByTypeTab());
        
        // Tab 3: Principals by State
        reportTabs.addTab("Principals by State", buildPrincipalsByStateTab());
        
        // Tab 4: Employees by Work History
        reportTabs.addTab("Employees by Work History", buildEmployeesByWorkHistoryTab());
        
        // Tab 5: Contractor Report (Architects & Owners)
        reportTabs.addTab("Contractors Report", buildContractorReportTab());
        
        // Tab 6: Principal Profitability by Sector
        reportTabs.addTab("Principal Profitability", buildPrincipalProfitabilityTab());
        
        // Tab 7: Project Profitability by Material
        reportTabs.addTab("Profitability by Material", buildMaterialProfitabilityTab());
        
        // Tab 8: Project Profitability by Architect
        reportTabs.addTab("Architect Profitability", buildArchitectProfitabilityTab());
        
        // Tab 9: Project Profitability by Owner
        reportTabs.addTab("Owner Profitability", buildOwnerProfitabilityTab());
        
        // Tab 10: Most Profitable PM
        reportTabs.addTab("Top PM (Type/Material)", buildProfitablePMTab());
        
        // Tab 11: Employees by Owner
        reportTabs.addTab("Employees by Owner", buildEmployeesForOwnerTab());
        
        // Tab 12: Architects with Multiple Projects
        reportTabs.addTab("Multi-Project Architects", buildArchitectMultipleTab());
        
        // Tab 13: Engineers by Project Manager
        reportTabs.addTab("Engineers by PM", buildEngineersForPMTab());
        
        // Tab 14: Project Location by Owner & PM
        reportTabs.addTab("Project Location", buildProjectLocationTab());
        
        add(reportTabs, BorderLayout.CENTER);
    }

    // ──────────────────────────────────────────────────────────────────
    // Tab 1: Employee Directory
    // ──────────────────────────────────────────────────────────────────
    private JPanel buildEmployeeDirectoryTab() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        JButton generateBtn = new JButton("Generate Directory");
        generateBtn.addActionListener(e -> generateEmployeeDirectory());
        
        String[] columns = {"ID", "Full Name", "Rank", "Cost Rate"};
        employeeDirectoryTable = new JTable(new Object[][]{}, columns);
        JScrollPane scrollPane = new JScrollPane(employeeDirectoryTable);
        
        panel.add(generateBtn, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }

    private void generateEmployeeDirectory() {
        try {
            List<Employee> employees = employeeDAO.getAllEmployees();
            Object[][] data = new Object[employees.size()][4];
            
            for (int i = 0; i < employees.size(); i++) {
                Employee emp = employees.get(i);
                data[i][0] = emp.getEmployeeId();
                data[i][1] = emp.getFullName();
                data[i][2] = emp.getRankName();
                data[i][3] = "$" + String.format("%.2f", emp.getCostRate());
            }
            
            employeeDirectoryTable.setModel(new javax.swing.table.DefaultTableModel(data,
                new String[]{"ID", "Full Name", "Rank", "Cost Rate"}));
            parent.setStatus("Employee directory generated.");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // ──────────────────────────────────────────────────────────────────
    // Tab 2: Projects by Type
    // Tab 2: Projects by Type/Location (dual filter)
    // ──────────────────────────────────────────────────────────────────
    private JPanel buildProjectsByTypeTab() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        filterPanel.add(new JLabel("Sector:"));
        JComboBox<String> sectorCombo = new JComboBox<>(
            new String[]{"All", "Residential", "Office", "Mixed Use", "Repair", "Renovation"}
        );
        sectorCombo.setSelectedItem("All");
        filterPanel.add(sectorCombo);
        
        filterPanel.add(new JLabel("  Location:"));
        JComboBox<String> locationCombo = new JComboBox<>();
        locationCombo.addItem("All");
        List<Location> locations = locationDAO.getAllLocations();
        for (Location loc : locations) {
            locationCombo.addItem(loc.getState());
        }
        filterPanel.add(locationCombo);
        
        JButton generateBtn = new JButton("Filter");
        generateBtn.addActionListener(e -> {
            String sector = (String) sectorCombo.getSelectedItem();
            String state = (String) locationCombo.getSelectedItem();
            generateProjectsByTypeAndLocation(sector, state);
        });
        filterPanel.add(generateBtn);
        
        String[] columns = {"ID", "Name", "Sector", "Material", "Location", "Fee"};
        projectsTable = new JTable(new Object[][]{}, columns);
        JScrollPane scrollPane = new JScrollPane(projectsTable);
        
        panel.add(filterPanel, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }

    private void generateProjectsByTypeAndLocation(String sector, String state) {
        try {
            
            // Build locationId -> state name map
            Map<Integer, String> locationMap = new HashMap<>();
            List<Location> locations = locationDAO.getAllLocations();
            for (Location loc : locations) {
                locationMap.put(loc.getLocationId(), loc.getState());
            }
            
            List<Project> projects = new java.util.ArrayList<>();
            
            // Determine which filter to apply
            boolean filterBySector = !sector.equals("All");
            boolean filterByState = !state.equals("All");
            
            if (filterBySector && filterByState) {
                // Filter by both sector and state
                List<Project> bySector = projectDAO.getProjectsBySector(sector);
                for (Project p : bySector) {
                    String pState = locationMap.getOrDefault(p.getLocationId(), "");
                    if (pState.equals(state)) {
                        projects.add(p);
                    }
                }
            } else if (filterBySector) {
                // Filter by sector only
                projects = projectDAO.getProjectsBySector(sector);
            } else if (filterByState) {
                // Filter by state only
                projects = projectDAO.getProjectsByState(state);
            } else {
                // No filter, get all projects
                projects = projectDAO.getAllProjects();
            }
            
            Object[][] data = new Object[projects.size()][6];
            
            for (int i = 0; i < projects.size(); i++) {
                Project p = projects.get(i);
                String locationName = locationMap.getOrDefault(p.getLocationId(), "Unknown");
                data[i][0] = p.getProjectId();
                data[i][1] = p.getProjectName();
                data[i][2] = p.getSector();
                data[i][3] = p.getMaterial();
                data[i][4] = locationName;
                data[i][5] = "$" + String.format("%.2f", p.getFee());
            }
            
            projectsTable.setModel(new javax.swing.table.DefaultTableModel(data,
                new String[]{"ID", "Name", "Sector", "Material", "Location", "Fee"}));
            
            String filterDesc = "";
            if (!sector.equals("All")) filterDesc += sector;
            if (!state.equals("All")) filterDesc += (filterDesc.isEmpty() ? "" : " / ") + state;
            if (filterDesc.isEmpty()) filterDesc = "All";
            parent.setStatus("Projects " + filterDesc + " generated.");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // ──────────────────────────────────────────────────────────────────
    // Tab 3: Principals by State
    // ──────────────────────────────────────────────────────────────────
    private JPanel buildPrincipalsByStateTab() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        filterPanel.add(new JLabel("State:"));
        JComboBox<String> stateCombo = new JComboBox<>();
        loadStates(stateCombo);
        filterPanel.add(stateCombo);
        
        JButton generateBtn = new JButton("Find Principals");
        generateBtn.addActionListener(e -> {
            String state = (String) stateCombo.getSelectedItem();
            generatePrincipalsByState(state);
        });
        filterPanel.add(generateBtn);
        
        String[] columns = {"ID", "Name", "Cost Rate"};
        principalsTable = new JTable(new Object[][]{}, columns);
        JScrollPane scrollPane = new JScrollPane(principalsTable);
        
        panel.add(filterPanel, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }
    
    private void loadStates(JComboBox<String> stateCombo) {
        stateCombo.removeAllItems();
        List<Location> locations = locationDAO.getAllLocations();
        for (Location loc : locations) {
            stateCombo.addItem(loc.getState());
        }
    }

    private void generatePrincipalsByState(String state) {
        try {
            List<Employee> principals = employeeDAO.getPrincipalsByState(state);
            Object[][] data = new Object[principals.size()][3];
            
            for (int i = 0; i < principals.size(); i++) {
                Employee emp = principals.get(i);
                data[i][0] = emp.getEmployeeId();
                data[i][1] = emp.getFullName();
                data[i][2] = "$" + String.format("%.2f", emp.getCostRate());
            }
            
            principalsTable.setModel(new javax.swing.table.DefaultTableModel(data,
                new String[]{"ID", "Name", "Cost Rate"}));
            parent.setStatus("Principals in " + state + " listed.");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // ──────────────────────────────────────────────────────────────────
    // Tab 4: Employees by Work History
    // ──────────────────────────────────────────────────────────────────
    private JPanel buildEmployeesByWorkHistoryTab() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        filterPanel.add(new JLabel("Sector:"));
        JComboBox<String> sectorCombo = new JComboBox<>(
            new String[]{null, "Residential", "Office", "Mixed Use", "Repair", "Renovation"}
        );
        sectorCombo.setSelectedItem(null);
        filterPanel.add(sectorCombo);
        
        filterPanel.add(new JLabel("  Material:"));
        JComboBox<String> materialCombo = new JComboBox<>(
            new String[]{null, "Concrete", "Steel", "Wood"}
        );
        materialCombo.setSelectedItem(null);
        filterPanel.add(materialCombo);
        
        JButton generateBtn = new JButton("Search Employees");
        generateBtn.addActionListener(e -> {
            String sector = (String) sectorCombo.getSelectedItem();
            String material = (String) materialCombo.getSelectedItem();
            generateEmployeesByWorkHistory(sector, material);
        });
        filterPanel.add(generateBtn);
        
        String[] columns = {"ID", "Name", "Rank", "Cost Rate"};
        workHistoryTable = new JTable(new Object[][]{}, columns);
        JScrollPane scrollPane = new JScrollPane(workHistoryTable);
        
        panel.add(filterPanel, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }

    private void generateEmployeesByWorkHistory(String sector, String material) {
        try {
            List<Employee> employees = employeeDAO.getEmployeesByWorkHistory(sector, material);
            Object[][] data = new Object[employees.size()][4];
            
            for (int i = 0; i < employees.size(); i++) {
                Employee emp = employees.get(i);
                data[i][0] = emp.getEmployeeId();
                data[i][1] = emp.getFullName();
                data[i][2] = emp.getRankName();
                data[i][3] = "$" + String.format("%.2f", emp.getCostRate());
            }
            
            workHistoryTable.setModel(new javax.swing.table.DefaultTableModel(data,
                new String[]{"ID", "Name", "Rank", "Cost Rate"}));
            
            String filterStr = (sector != null ? sector : "") + " " + (material != null ? material : "");
            parent.setStatus("Employees with work history in " + filterStr + " listed.");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // ──────────────────────────────────────────────────────────────────
    // Tab 5: Architect & Owner Report
    // ──────────────────────────────────────────────────────────────────
    private JPanel buildContractorReportTab() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        filterPanel.add(new JLabel("Type:"));
        JComboBox<String> typeCombo = new JComboBox<>(
            new String[]{"Both", "Architect", "Owner"}
        );
        filterPanel.add(typeCombo);
        
        JButton generateBtn = new JButton("Generate Report");
        generateBtn.addActionListener(e -> {
            String type = (String) typeCombo.getSelectedItem();
            generateContractorReport(type);
        });
        filterPanel.add(generateBtn);

        JButton multiBtn = new JButton("Multi-Project Contractors");
        multiBtn.addActionListener(e -> generateMultiProjectContractors());
        filterPanel.add(multiBtn);
        
        String[] columns = {"ID", "Name", "Type"};
        contractorTable = new JTable(new Object[][]{}, columns);
        JScrollPane scrollPane = new JScrollPane(contractorTable);
        
        panel.add(filterPanel, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }

    private void generateContractorReport(String type) {
        try {
            Object[][] data = null;
            
            if (type.equals("Both")) {
                String sql = "SELECT contractor_id, contractor_name, contractor_type FROM ( " +
                             "    SELECT DISTINCT 'A' AS contractor_prefix, a.architect_id AS contractor_id, a.name AS contractor_name, 'Architect' AS contractor_type " +
                             "    FROM Architect a " +
                             "    JOIN Project p ON p.architect_id = a.architect_id " +
                             "    UNION ALL " +
                             "    SELECT DISTINCT 'O' AS contractor_prefix, o.owner_id AS contractor_id, o.name AS contractor_name, 'Owner' AS contractor_type " +
                             "    FROM Owner o " +
                             "    JOIN Project p ON p.owner_id = o.owner_id " +
                             ") c " +
                             "GROUP BY contractor_type, contractor_id, contractor_name " +
                             "ORDER BY contractor_type, contractor_name";
                java.sql.ResultSet rs = DatabaseManager.getConnection().createStatement().executeQuery(sql);
                java.util.List<Object[]> rows = new java.util.ArrayList<>();
                while (rs.next()) {
                    String prefix = rs.getString("contractor_type").equals("Architect") ? "A" : "O";
                    rows.add(new Object[]{
                        prefix + rs.getInt("contractor_id"),
                        rs.getString("contractor_name"),
                        rs.getString("contractor_type")
                    });
                }
                data = rows.toArray(new Object[0][]);
            } else if (type.equals("Architect")) {
                String sql = "SELECT DISTINCT a.architect_id, a.name " +
                             "FROM Architect a " +
                             "JOIN Project p ON p.architect_id = a.architect_id " +
                             "ORDER BY a.name";
                java.sql.ResultSet rs = DatabaseManager.getConnection().createStatement().executeQuery(sql);
                java.util.List<Object[]> rows = new java.util.ArrayList<>();
                while (rs.next()) {
                    rows.add(new Object[]{"A" + rs.getInt("architect_id"), rs.getString("name"), "Architect"});
                }
                data = rows.toArray(new Object[0][]);
            } else if (type.equals("Owner")) {
                String sql = "SELECT DISTINCT o.owner_id, o.name " +
                             "FROM Owner o " +
                             "JOIN Project p ON p.owner_id = o.owner_id " +
                             "ORDER BY o.name";
                java.sql.ResultSet rs = DatabaseManager.getConnection().createStatement().executeQuery(sql);
                java.util.List<Object[]> rows = new java.util.ArrayList<>();
                while (rs.next()) {
                    rows.add(new Object[]{"O" + rs.getInt("owner_id"), rs.getString("name"), "Owner"});
                }
                data = rows.toArray(new Object[0][]);
            }
            
            contractorTable.setModel(new javax.swing.table.DefaultTableModel(data,
                new String[]{"ID", "Name", "Type"}));
            parent.setStatus("Contractor report (" + type + ") generated.");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void generateMultiProjectContractors() {
        try {
            String sql =
                "SELECT contractor_id, contractor_name, contractor_type, COUNT(DISTINCT project_name) AS project_count, " +
                "GROUP_CONCAT(project_name ORDER BY project_name SEPARATOR ', ') AS projects " +
                "FROM ( " +
                "    SELECT a.architect_id AS contractor_id, a.name AS contractor_name, " +
                "           'Architect' AS contractor_type, p.name AS project_name " +
                "    FROM Architect a " +
                "    JOIN Project p ON p.architect_id = a.architect_id " +
                "    UNION ALL " +
                "    SELECT o.owner_id AS contractor_id, o.name AS contractor_name, " +
                "           'Owner' AS contractor_type, p.name AS project_name " +
                "    FROM Owner o " +
                "    JOIN Project p ON p.owner_id = o.owner_id " +
                ") x " +
                "GROUP BY contractor_type, contractor_id, contractor_name " +
                "HAVING COUNT(*) > 1 " +
                "ORDER BY project_count DESC, contractor_type, contractor_name";

            java.sql.ResultSet rs = DatabaseManager.getConnection().createStatement().executeQuery(sql);
            java.util.List<Object[]> rows = new java.util.ArrayList<>();
            while (rs.next()) {
                String prefix = rs.getString("contractor_type").equals("Architect") ? "A" : "O";
                rows.add(new Object[]{
                    prefix + rs.getInt("contractor_id"),
                    rs.getString("contractor_name"),
                    rs.getString("contractor_type"),
                    rs.getInt("project_count"),
                    rs.getString("projects")
                });
            }

            Object[][] data = rows.toArray(new Object[0][]);
            contractorTable.setModel(new javax.swing.table.DefaultTableModel(data,
                new String[]{"ID", "Name", "Type", "Project Count", "Projects"}));
            parent.setStatus("Multi-project contractors report generated.");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // ──────────────────────────────────────────────────────────────────
    // Tab 6: Principal Profitability by Sector
    // ──────────────────────────────────────────────────────────────────
    private JPanel buildPrincipalProfitabilityTab() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        JButton generateBtn = new JButton("Generate Report");
        generateBtn.addActionListener(e -> generatePrincipalProfitability());
        
        String[] columns = {"Principal", "Sector", "Project", "Fee", "Total Cost", "Profitability"};
        principalProfitabilityTable = new JTable(new Object[][]{}, columns);
        JScrollPane scrollPane = new JScrollPane(principalProfitabilityTable);
        
        panel.add(generateBtn, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }

    private void generatePrincipalProfitability() {
        try {
            String sql = "SELECT e.first_name, e.last_name, p.sector, p.name, p.fee, " +
                         "COALESCE(SUM(pe.man_hours * r.rank_cost), 0) as total_cost " +
                         "FROM Employee e " +
                         "JOIN Project p ON e.employee_id = p.principal_id " +
                         "LEFT JOIN Project_Employee pe ON p.project_id = pe.project_id " +
                         "LEFT JOIN Employee_Rank r ON (SELECT rank_id FROM Employee WHERE employee_id = pe.employee_id) = r.rank_id " +
                         "WHERE (SELECT rank_name FROM Employee_Rank WHERE rank_id = e.rank_id) = 'Principal' " +
                         "GROUP BY e.employee_id, p.project_id, p.sector " +
                         "ORDER BY e.last_name, p.sector";
            
            java.sql.ResultSet rs = DatabaseManager.getConnection().createStatement().executeQuery(sql);
            java.util.List<Object[]> rows = new java.util.ArrayList<>();
            while (rs.next()) {
                double fee = rs.getDouble("fee");
                double totalCost = rs.getDouble("total_cost");
                rows.add(new Object[]{
                    rs.getString("first_name") + " " + rs.getString("last_name"),
                    rs.getString("sector"),
                    rs.getString("name"),
                    "$" + String.format("%.2f", fee),
                    "$" + String.format("%.2f", totalCost),
                    "$" + String.format("%.2f", fee - totalCost)
                });
            }
            
            Object[][] data = rows.toArray(new Object[0][]);
            principalProfitabilityTable.setModel(new javax.swing.table.DefaultTableModel(data,
                new String[]{"Principal", "Sector", "Project", "Fee", "Total Cost", "Profitability"}));
            parent.setStatus("Principal profitability report generated.");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // ──────────────────────────────────────────────────────────────────
    // Tab 7: Project Profitability by Material
    // ──────────────────────────────────────────────────────────────────
    private JPanel buildMaterialProfitabilityTab() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        JButton generateBtn = new JButton("Generate Report");
        generateBtn.addActionListener(e -> generateMaterialProfitability());
        
        String[] columns = {"Material", "Project", "Fee", "Total Cost", "Profitability"};
        materialProfitabilityTable = new JTable(new Object[][]{}, columns);
        JScrollPane scrollPane = new JScrollPane(materialProfitabilityTable);
        
        panel.add(generateBtn, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }

    private void generateMaterialProfitability() {
        try {
            String sql = "SELECT p.material, p.name, p.fee, " +
                         "COALESCE(SUM(pe.man_hours * r.rank_cost), 0) as total_cost " +
                         "FROM Project p " +
                         "LEFT JOIN Project_Employee pe ON p.project_id = pe.project_id " +
                         "LEFT JOIN Employee_Rank r ON (SELECT rank_id FROM Employee WHERE employee_id = pe.employee_id) = r.rank_id " +
                         "GROUP BY p.material, p.project_id " +
                         "ORDER BY p.material";
            
            java.sql.ResultSet rs = DatabaseManager.getConnection().createStatement().executeQuery(sql);
            java.util.List<Object[]> rows = new java.util.ArrayList<>();
            while (rs.next()) {
                double fee = rs.getDouble("fee");
                double totalCost = rs.getDouble("total_cost");
                rows.add(new Object[]{
                    rs.getString("material"),
                    rs.getString("name"),
                    "$" + String.format("%.2f", fee),
                    "$" + String.format("%.2f", totalCost),
                    "$" + String.format("%.2f", fee - totalCost)
                });
            }
            
            Object[][] data = rows.toArray(new Object[0][]);
            materialProfitabilityTable.setModel(new javax.swing.table.DefaultTableModel(data,
                new String[]{"Material", "Project", "Fee", "Total Cost", "Profitability"}));
            parent.setStatus("Material profitability report generated.");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // ──────────────────────────────────────────────────────────────────
    // Tab 8: Project Profitability by Architect
    // ──────────────────────────────────────────────────────────────────
    private JPanel buildArchitectProfitabilityTab() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        JButton generateBtn = new JButton("Generate Report");
        generateBtn.addActionListener(e -> generateArchitectProfitability());
        
        String[] columns = {"Architect", "Project", "Fee", "Total Cost", "Profitability"};
        architectProfitabilityTable = new JTable(new Object[][]{}, columns);
        JScrollPane scrollPane = new JScrollPane(architectProfitabilityTable);
        
        panel.add(generateBtn, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }

    private void generateArchitectProfitability() {
        try {
            String sql = "SELECT a.name, p.name as project_name, p.fee, " +
                         "COALESCE(SUM(pe.man_hours * r.rank_cost), 0) as total_cost " +
                         "FROM Architect a " +
                         "JOIN Project p ON a.architect_id = p.architect_id " +
                         "LEFT JOIN Project_Employee pe ON p.project_id = pe.project_id " +
                         "LEFT JOIN Employee_Rank r ON (SELECT rank_id FROM Employee WHERE employee_id = pe.employee_id) = r.rank_id " +
                         "GROUP BY a.architect_id, p.project_id " +
                         "ORDER BY a.name";
            
            java.sql.ResultSet rs = DatabaseManager.getConnection().createStatement().executeQuery(sql);
            java.util.List<Object[]> rows = new java.util.ArrayList<>();
            while (rs.next()) {
                double fee = rs.getDouble("fee");
                double totalCost = rs.getDouble("total_cost");
                rows.add(new Object[]{
                    rs.getString("name"),
                    rs.getString("project_name"),
                    "$" + String.format("%.2f", fee),
                    "$" + String.format("%.2f", totalCost),
                    "$" + String.format("%.2f", fee - totalCost)
                });
            }
            
            Object[][] data = rows.toArray(new Object[0][]);
            architectProfitabilityTable.setModel(new javax.swing.table.DefaultTableModel(data,
                new String[]{"Architect", "Project", "Fee", "Total Cost", "Profitability"}));
            parent.setStatus("Architect profitability report generated.");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // ──────────────────────────────────────────────────────────────────
    // Tab 9: Project Profitability by Owner
    // ──────────────────────────────────────────────────────────────────
    private JPanel buildOwnerProfitabilityTab() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        JButton generateBtn = new JButton("Generate Report");
        generateBtn.addActionListener(e -> generateOwnerProfitability());
        
        String[] columns = {"Owner", "Project", "Fee", "Total Cost", "Profitability"};
        ownerProfitabilityTable = new JTable(new Object[][]{}, columns);
        JScrollPane scrollPane = new JScrollPane(ownerProfitabilityTable);
        
        panel.add(generateBtn, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }

    private void generateOwnerProfitability() {
        try {
            String sql = "SELECT o.name, p.name as project_name, p.fee, " +
                         "COALESCE(SUM(pe.man_hours * r.rank_cost), 0) as total_cost " +
                         "FROM Owner o " +
                         "JOIN Project p ON o.owner_id = p.owner_id " +
                         "LEFT JOIN Project_Employee pe ON p.project_id = pe.project_id " +
                         "LEFT JOIN Employee_Rank r ON (SELECT rank_id FROM Employee WHERE employee_id = pe.employee_id) = r.rank_id " +
                         "GROUP BY o.owner_id, p.project_id " +
                         "ORDER BY o.name";
            
            java.sql.ResultSet rs = DatabaseManager.getConnection().createStatement().executeQuery(sql);
            java.util.List<Object[]> rows = new java.util.ArrayList<>();
            while (rs.next()) {
                double fee = rs.getDouble("fee");
                double totalCost = rs.getDouble("total_cost");
                rows.add(new Object[]{
                    rs.getString("name"),
                    rs.getString("project_name"),
                    "$" + String.format("%.2f", fee),
                    "$" + String.format("%.2f", totalCost),
                    "$" + String.format("%.2f", fee - totalCost)
                });
            }
            
            Object[][] data = rows.toArray(new Object[0][]);
            ownerProfitabilityTable.setModel(new javax.swing.table.DefaultTableModel(data,
                new String[]{"Owner", "Project", "Fee", "Total Cost", "Profitability"}));
            parent.setStatus("Owner profitability report generated.");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // ──────────────────────────────────────────────────────────────────
    // Tab 10: Most Profitable PM (Residential/Wood)
    // ──────────────────────────────────────────────────────────────────
    private JPanel buildProfitablePMTab() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        filterPanel.add(new JLabel("Sector:"));
        JComboBox<String> sectorCombo = new JComboBox<>(
            new String[]{"All", "Residential", "Office", "Mixed Use", "Repair", "Renovation"}
        );
        filterPanel.add(sectorCombo);
        
        filterPanel.add(new JLabel("  Material:"));
        JComboBox<String> materialCombo = new JComboBox<>(
            new String[]{"All", "Concrete", "Steel", "Wood"}
        );
        filterPanel.add(materialCombo);
        
        JButton generateBtn = new JButton("Find Most Profitable PM");
        generateBtn.addActionListener(e -> {
            String sector = (String) sectorCombo.getSelectedItem();
            String material = (String) materialCombo.getSelectedItem();
            generateProfitablePM(sector, material);
        });
        filterPanel.add(generateBtn);
        
        String[] columns = {"Project Manager", "Projects Count", "Total Profitability"};
        profitablePMTable = new JTable(new Object[][]{}, columns);
        JScrollPane scrollPane = new JScrollPane(profitablePMTable);
        
        panel.add(filterPanel, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }

    private void generateProfitablePM(String sector, String material) {
        try {
            // Build dynamic query based on filters
            String whereClause = "";
            if (!sector.equals("All")) {
                whereClause += " AND p.sector = '" + sector + "'";
            }
            if (!material.equals("All")) {
                whereClause += " AND p.material = '" + material + "'";
            }
            
            // Use a subquery to calculate total cost per project, then profit per PM
            String sql = "SELECT e.employee_id, e.first_name, e.last_name, COUNT(p.project_id) as project_count, " +
                         "SUM(p.fee) - SUM(COALESCE(mh.total_hours_cost, 0)) as total_profit " +
                         "FROM Employee e " +
                         "JOIN Project p ON e.employee_id = p.project_manager_id " +
                         "LEFT JOIN ( " +
                         "  SELECT project_id, SUM(man_hours * rank_cost) as total_hours_cost " +
                         "  FROM Project_Employee pe " +
                         "  JOIN Employee emp ON pe.employee_id = emp.employee_id " +
                         "  JOIN Employee_Rank r ON emp.rank_id = r.rank_id " +
                         "  GROUP BY project_id " +
                         ") mh ON p.project_id = mh.project_id " +
                         "WHERE 1=1" + whereClause +
                         " GROUP BY e.employee_id " +
                         " ORDER BY total_profit DESC LIMIT 1";
            
            java.sql.ResultSet rs = DatabaseManager.getConnection().createStatement().executeQuery(sql);
            java.util.List<Object[]> rows = new java.util.ArrayList<>();
            if (rs.next()) {
                rows.add(new Object[]{
                    rs.getString("first_name") + " " + rs.getString("last_name"),
                    rs.getInt("project_count"),
                    "$" + String.format("%.2f", rs.getDouble("total_profit"))
                });
            } else {
                rows.add(new Object[]{"No data", 0, "$0.00"});
            }
            
            Object[][] data = rows.toArray(new Object[0][]);
            profitablePMTable.setModel(new javax.swing.table.DefaultTableModel(data,
                new String[]{"Project Manager", "Projects Count", "Total Profitability"}));
            parent.setStatus("Most profitable PM report generated.");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // ──────────────────────────────────────────────────────────────────
    // Tab 11: Employees by Owner
    // ──────────────────────────────────────────────────────────────────
    private JPanel buildEmployeesForOwnerTab() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        filterPanel.add(new JLabel("Owner:"));
        employeesForOwnerCombo = new JComboBox<>();
        List<Owner> owners = ownerDAO.getAllOwners();
        for (Owner o : owners) {
            employeesForOwnerCombo.addItem(o.getName());
        }
        filterPanel.add(employeesForOwnerCombo);
        
        JButton generateBtn = new JButton("Generate Report");
        generateBtn.addActionListener(e -> {
            String ownerName = (String) employeesForOwnerCombo.getSelectedItem();
            generateEmployeesForOwner(ownerName);
        });
        filterPanel.add(generateBtn);
        
        String[] columns = {"Employee Name", "Rank", "Project"};
        employeesForOwnerTable = new JTable(new Object[][]{}, columns);
        JScrollPane scrollPane = new JScrollPane(employeesForOwnerTable);
        
        panel.add(filterPanel, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }

    private void generateEmployeesForOwner(String ownerName) {
        try {
            String sql = "SELECT DISTINCT e.first_name, e.last_name, er.rank_name, p.name " +
                         "FROM Employee e " +
                         "JOIN Project_Employee pe ON e.employee_id = pe.employee_id " +
                         "JOIN Project p ON pe.project_id = p.project_id " +
                         "JOIN Owner o ON p.owner_id = o.owner_id " +
                         "JOIN Employee_Rank er ON e.rank_id = er.rank_id " +
                         "WHERE o.name = ? " +
                         "ORDER BY e.last_name";
            
            java.sql.PreparedStatement ps = DatabaseManager.getConnection().prepareStatement(sql);
            ps.setString(1, ownerName);
            java.sql.ResultSet rs = ps.executeQuery();
            java.util.List<Object[]> rows = new java.util.ArrayList<>();
            while (rs.next()) {
                rows.add(new Object[]{
                    rs.getString("first_name") + " " + rs.getString("last_name"),
                    rs.getString("rank_name"),
                    rs.getString("name")
                });
            }
            
            Object[][] data = rows.toArray(new Object[0][]);
            employeesForOwnerTable.setModel(new javax.swing.table.DefaultTableModel(data,
                new String[]{"Employee Name", "Rank", "Project"}));
            parent.setStatus("Employees for owner report generated.");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // ──────────────────────────────────────────────────────────────────
    // Tab 12: Architects with Multiple Projects
    // ──────────────────────────────────────────────────────────────────
    private JPanel buildArchitectMultipleTab() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        JButton generateBtn = new JButton("Generate Report");
        generateBtn.addActionListener(e -> generateArchitectMultiple());
        
        String[] columns = {"Architect", "Project Count", "Projects"};
        architectMultipleTable = new JTable(new Object[][]{}, columns);
        JScrollPane scrollPane = new JScrollPane(architectMultipleTable);
        
        panel.add(generateBtn, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }

    private void generateArchitectMultiple() {
        try {
            String sql = "SELECT a.name, COUNT(p.project_id) as project_count, " +
                         "GROUP_CONCAT(p.name SEPARATOR ', ') as projects " +
                         "FROM Architect a " +
                         "JOIN Project p ON a.architect_id = p.architect_id " +
                         "GROUP BY a.architect_id " +
                         "HAVING COUNT(p.project_id) > 1 " +
                         "ORDER BY project_count DESC";
            
            java.sql.ResultSet rs = DatabaseManager.getConnection().createStatement().executeQuery(sql);
            java.util.List<Object[]> rows = new java.util.ArrayList<>();
            while (rs.next()) {
                rows.add(new Object[]{
                    rs.getString("name"),
                    rs.getInt("project_count"),
                    rs.getString("projects")
                });
            }
            
            Object[][] data = rows.toArray(new Object[0][]);
            architectMultipleTable.setModel(new javax.swing.table.DefaultTableModel(data,
                new String[]{"Architect", "Project Count", "Projects"}));
            parent.setStatus("Multi-project architects report generated.");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // ──────────────────────────────────────────────────────────────────
    // Tab 13: Engineers by Project Manager
    // ──────────────────────────────────────────────────────────────────
    private JPanel buildEngineersForPMTab() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        filterPanel.add(new JLabel("Project Manager:"));
        engineersForPMCombo = new JComboBox<>();
        List<Employee> pms = employeeDAO.getProjectManagers();
        for (Employee pm : pms) {
            engineersForPMCombo.addItem(pm);
        }
        filterPanel.add(engineersForPMCombo);
        
        JButton generateBtn = new JButton("Generate Report");
        generateBtn.addActionListener(e -> {
            Employee pm = (Employee) engineersForPMCombo.getSelectedItem();
            if (pm != null) {
                generateEngineersForPM(pm.getFirstName(), pm.getLastName());
            }
        });
        filterPanel.add(generateBtn);
        
        String[] columns = {"Engineer Name", "Rank"};
        engineersForPMTable = new JTable(new Object[][]{}, columns);
        JScrollPane scrollPane = new JScrollPane(engineersForPMTable);
        
        panel.add(filterPanel, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }

    private void generateEngineersForPM(String pmFirstName, String pmLastName) {
        try {
            String sql = "SELECT DISTINCT e.first_name, e.last_name, er.rank_name " +
                         "FROM Employee e " +
                         "JOIN Project_Employee pe ON e.employee_id = pe.employee_id " +
                         "JOIN Project p ON pe.project_id = p.project_id " +
                         "JOIN Employee pm ON p.project_manager_id = pm.employee_id " +
                         "JOIN Employee_Rank er ON e.rank_id = er.rank_id " +
                         "WHERE pm.first_name = ? AND pm.last_name = ? " +
                         "  AND er.rank_name LIKE '%Engineer%' " +
                         "ORDER BY e.last_name";
            
            java.sql.PreparedStatement ps = DatabaseManager.getConnection().prepareStatement(sql);
            ps.setString(1, pmFirstName);
            ps.setString(2, pmLastName);
            java.sql.ResultSet rs = ps.executeQuery();
            java.util.List<Object[]> rows = new java.util.ArrayList<>();
            while (rs.next()) {
                rows.add(new Object[]{
                    rs.getString("first_name") + " " + rs.getString("last_name"),
                    rs.getString("rank_name")
                });
            }
            
            Object[][] data = rows.toArray(new Object[0][]);
            engineersForPMTable.setModel(new javax.swing.table.DefaultTableModel(data,
                new String[]{"Engineer Name", "Rank"}));
            parent.setStatus("Engineers for PM report generated.");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // ──────────────────────────────────────────────────────────────────
    // Tab 14: Project Location by Owner and PM
    // ──────────────────────────────────────────────────────────────────
    private JPanel buildProjectLocationTab() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        JPanel filterPanel = new JPanel(new GridLayout(3, 2, 5, 5));
        
        filterPanel.add(new JLabel("Owner:"));
        projectLocationOwnerCombo = new JComboBox<>();
        List<Owner> owners = ownerDAO.getAllOwners();
        for (Owner o : owners) {
            projectLocationOwnerCombo.addItem(o.getName());
        }
        filterPanel.add(projectLocationOwnerCombo);
        
        filterPanel.add(new JLabel("Project Manager:"));
        projectLocationPMCombo = new JComboBox<>();
        List<Employee> pms = employeeDAO.getProjectManagers();
        for (Employee pm : pms) {
            projectLocationPMCombo.addItem(pm);
        }
        filterPanel.add(projectLocationPMCombo);
        
        JButton generateBtn = new JButton("Find Location");
        generateBtn.addActionListener(e -> {
            String ownerName = (String) projectLocationOwnerCombo.getSelectedItem();
            Employee pm = (Employee) projectLocationPMCombo.getSelectedItem();
            if (pm != null) {
                generateProjectLocation(ownerName, pm.getFirstName(), pm.getLastName());
            }
        });
        filterPanel.add(generateBtn);
        
        String[] columns = {"Project", "Location", "Owner", "Project Manager"};
        projectLocationTable = new JTable(new Object[][]{}, columns);
        JScrollPane scrollPane = new JScrollPane(projectLocationTable);
        
        panel.add(filterPanel, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }

    private void generateProjectLocation(String ownerName, String pmFirstName, String pmLastName) {
        try {
            String sql = "SELECT p.name, l.state, o.name as owner_name, CONCAT(e.first_name, ' ', e.last_name) as pm_name " +
                         "FROM Project p " +
                         "JOIN Location l ON p.location_id = l.location_id " +
                         "JOIN Owner o ON p.owner_id = o.owner_id " +
                         "JOIN Employee e ON p.project_manager_id = e.employee_id " +
                         "WHERE o.name = ? AND e.first_name = ? AND e.last_name = ?";
            
            java.sql.PreparedStatement ps = DatabaseManager.getConnection().prepareStatement(sql);
            ps.setString(1, ownerName);
            ps.setString(2, pmFirstName);
            ps.setString(3, pmLastName);
            java.sql.ResultSet rs = ps.executeQuery();
            java.util.List<Object[]> rows = new java.util.ArrayList<>();
            while (rs.next()) {
                rows.add(new Object[]{
                    rs.getString("name"),
                    rs.getString("state"),
                    rs.getString("owner_name"),
                    rs.getString("pm_name")
                });
            }
            
            Object[][] data = rows.toArray(new Object[0][]);
            projectLocationTable.setModel(new javax.swing.table.DefaultTableModel(data,
                new String[]{"Project", "Location", "Owner", "Project Manager"}));
            parent.setStatus("Project location report generated.");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // ──────────────────────────────────────────────────────────────────
    // Refresh methods for runtime dropdown updates
    // ──────────────────────────────────────────────────────────────────

    public void refreshReportDropdowns() {
        refreshOwnerDropdowns();
        refreshProjectManagerDropdowns();
    }

    private void refreshOwnerDropdowns() {
        if (employeesForOwnerCombo != null) {
            String selected = (String) employeesForOwnerCombo.getSelectedItem();
            employeesForOwnerCombo.removeAllItems();
            List<Owner> owners = ownerDAO.getAllOwners();
            for (Owner o : owners) {
                employeesForOwnerCombo.addItem(o.getName());
            }
            if (selected != null) {
                employeesForOwnerCombo.setSelectedItem(selected);
            }
        }

        if (projectLocationOwnerCombo != null) {
            String selected = (String) projectLocationOwnerCombo.getSelectedItem();
            projectLocationOwnerCombo.removeAllItems();
            List<Owner> owners = ownerDAO.getAllOwners();
            for (Owner o : owners) {
                projectLocationOwnerCombo.addItem(o.getName());
            }
            if (selected != null) {
                projectLocationOwnerCombo.setSelectedItem(selected);
            }
        }
    }

    private void refreshProjectManagerDropdowns() {
        if (engineersForPMCombo != null) {
            Employee selected = (Employee) engineersForPMCombo.getSelectedItem();
            engineersForPMCombo.removeAllItems();
            List<Employee> pms = employeeDAO.getProjectManagers();
            for (Employee pm : pms) {
                engineersForPMCombo.addItem(pm);
            }
            if (selected != null && pms.contains(selected)) {
                engineersForPMCombo.setSelectedItem(selected);
            }
        }

        if (projectLocationPMCombo != null) {
            Employee selected = (Employee) projectLocationPMCombo.getSelectedItem();
            projectLocationPMCombo.removeAllItems();
            List<Employee> pms = employeeDAO.getProjectManagers();
            for (Employee pm : pms) {
                projectLocationPMCombo.addItem(pm);
            }
            if (selected != null && pms.contains(selected)) {
                projectLocationPMCombo.setSelectedItem(selected);
            }
        }
    }
}
