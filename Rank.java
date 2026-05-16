package ska.model;

/**
 * Represents a U.S. state or DC where SK&A operates.
 * Used primarily for principal licensing.
 */
public class Location {

    private int    locationId;
    private String state;  // e.g. "Maryland", "Virginia", "DC"

    public Location() {}

    public Location(int locationId, String state) {
        this.locationId = locationId;
        this.state = state;
    }

    // Getters
    public int    getLocationId()    { return locationId; }
    public String getState()         { return state; }

    // Setters
    public void setLocationId(int locationId)       { this.locationId = locationId; }
    public void setState(String state)              { this.state = state; }

    @Override
    public String toString() { return state; }
}
