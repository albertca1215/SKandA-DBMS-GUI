package ska.model;

/**
 * Represents an SK&A project.
 */
public class Project {

    private int    projectId;
    private String projectName;
    private String sector;       // Residential, Office, Mixed Use, Repair, Renovation, etc.
    private String material;     // Concrete, Steel, Wood
    private double fee;          // Contract fee negotiated by principal
    private int    locationId;   // Foreign key to Location (state where project is located)
    private String stateName;    // State name (populated from Location table)
    private int    ownerId;
    private int    architectId;
    private int    principalId;
    private int    projectManagerId;

    public Project() {}

    public Project(int projectId, String projectName, String sector,
                   String material, double fee, int locationId, String stateName,
                   int ownerId, int architectId, int principalId, int projectManagerId) {
        this.projectId        = projectId;
        this.projectName      = projectName;
        this.sector           = sector;
        this.material         = material;
        this.fee              = fee;
        this.locationId       = locationId;
        this.stateName        = stateName;
        this.ownerId          = ownerId;
        this.architectId      = architectId;
        this.principalId      = principalId;
        this.projectManagerId = projectManagerId;
    }

    // Getters
    public int    getProjectId()        { return projectId; }
    public String getProjectName()      { return projectName; }
    public String getSector()           { return sector; }
    public String getMaterial()         { return material; }
    public double getFee()              { return fee; }
    public int    getLocationId()       { return locationId; }
    public String getStateName()        { return stateName; }
    public int    getOwnerId()          { return ownerId; }
    public int    getArchitectId()      { return architectId; }
    public int    getPrincipalId()      { return principalId; }
    public int    getProjectManagerId() { return projectManagerId; }

    // Setters
    public void setProjectId(int projectId)               { this.projectId        = projectId; }
    public void setProjectName(String projectName)        { this.projectName      = projectName; }
    public void setSector(String sector)                  { this.sector           = sector; }
    public void setMaterial(String material)              { this.material         = material; }
    public void setFee(double fee)                        { this.fee              = fee; }
    public void setLocationId(int locationId)             { this.locationId       = locationId; }
    public void setStateName(String stateName)            { this.stateName        = stateName; }
    public void setOwnerId(int ownerId)                   { this.ownerId          = ownerId; }
    public void setArchitectId(int architectId)           { this.architectId      = architectId; }
    public void setPrincipalId(int principalId)           { this.principalId      = principalId; }
    public void setProjectManagerId(int projectManagerId) { this.projectManagerId = projectManagerId; }

    @Override
    public String toString() { return projectName + " [" + sector + " | " + stateName + "]"; }
}