package ska.model;

/**
 * Represents an SK&A employee.
 * Ranks (low to high): Project Engineer 1-3, Assistant Project Manager,
 * Project Manager, Associate, Principal
 */
public class Employee {

    private int    employeeId;
    private String firstName;
    private String lastName;
    private int    rankId;        // Foreign key to Employee_Rank table
    private String rankName;      // Display name: e.g. "Project Engineer 1", "Principal"
    private double costRate;      // Hourly cost based on rank

    public Employee() {}

    public Employee(int employeeId, String firstName, String lastName,
                    int rankId, String rankName, double costRate) {
        this.employeeId = employeeId;
        this.firstName  = firstName;
        this.lastName   = lastName;
        this.rankId     = rankId;
        this.rankName   = rankName;
        this.costRate   = costRate;
    }

    // Getters
    public int    getEmployeeId() { return employeeId; }
    public String getFirstName()  { return firstName; }
    public String getLastName()   { return lastName; }
    public String getFullName()   { return firstName + " " + lastName; }
    public int    getRankId()     { return rankId; }
    public String getRankName()   { return rankName; }
    public double getCostRate()   { return costRate; }

    // Setters
    public void setEmployeeId(int employeeId)    { this.employeeId = employeeId; }
    public void setFirstName(String firstName)   { this.firstName  = firstName; }
    public void setLastName(String lastName)     { this.lastName   = lastName; }
    public void setRankId(int rankId)            { this.rankId     = rankId; }
    public void setRankName(String rankName)     { this.rankName   = rankName; }
    public void setCostRate(double costRate)     { this.costRate   = costRate; }

    @Override
    public String toString() { return getFullName() + " (" + rankName + ")"; }
}