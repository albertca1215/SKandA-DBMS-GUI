package ska.model;

/**
 * Represents the man-hours worked by an employee on a specific project.
 * Links Employee ↔ Project with hours worked and role clarification.
 */
public class ManHours {

    private int projectEmployeeId;
    private int projectId;
    private int employeeId;
    private double manHours;      // Number of hours worked on this project
    private String roleOnProject; // e.g. 'Engineer', 'Project Manager', 'Principal' (clarifies APM role)

    public ManHours() {}

    public ManHours(int projectEmployeeId, int projectId, int employeeId, double manHours, String roleOnProject) {
        this.projectEmployeeId = projectEmployeeId;
        this.projectId = projectId;
        this.employeeId = employeeId;
        this.manHours = manHours;
        this.roleOnProject = roleOnProject;
    }

    // Getters
    public int    getProjectEmployeeId() { return projectEmployeeId; }
    public int    getProjectId()         { return projectId; }
    public int    getEmployeeId()        { return employeeId; }
    public double getManHours()          { return manHours; }
    public String getRoleOnProject()     { return roleOnProject; }

    // Setters
    public void setProjectEmployeeId(int projectEmployeeId) { this.projectEmployeeId = projectEmployeeId; }
    public void setProjectId(int projectId)                 { this.projectId = projectId; }
    public void setEmployeeId(int employeeId)               { this.employeeId = employeeId; }
    public void setManHours(double manHours)                { this.manHours = manHours; }
    public void setRoleOnProject(String roleOnProject)      { this.roleOnProject = roleOnProject; }

    @Override
    public String toString() { return "Project " + projectId + " - Employee " + employeeId + ": " + manHours + " hrs (" + roleOnProject + ")"; }
}
