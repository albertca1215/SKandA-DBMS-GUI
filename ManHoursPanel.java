package ska.model;

/**
 * Represents a Principal's license to work in a specific state.
 * Links Employee (Principal only) ↔ Location (State).
 */
public class PrincipalLicense {

    private int employeeId;  // Must be a Principal rank
    private int locationId;  // The state they're licensed in

    public PrincipalLicense() {}

    public PrincipalLicense(int employeeId, int locationId) {
        this.employeeId = employeeId;
        this.locationId = locationId;
    }

    // Getters
    public int getEmployeeId()         { return employeeId; }
    public int getLocationId()         { return locationId; }

    // Setters
    public void setEmployeeId(int employeeId)                 { this.employeeId = employeeId; }
    public void setLocationId(int locationId)                 { this.locationId = locationId; }

    @Override
    public String toString() { return "Principal " + employeeId + " - Location " + locationId; }
}
