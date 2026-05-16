package ska.model;

/**
 * Represents an Owner who contracts with SK&A.
 */
public class Owner {

    private int    ownerId;
    private String name;

    public Owner() {}

    public Owner(int ownerId, String name) {
        this.ownerId = ownerId;
        this.name = name;
    }

    // Getters
    public int    getOwnerId() { return ownerId; }
    public String getName()    { return name; }

    // Setters
    public void setOwnerId(int ownerId)    { this.ownerId = ownerId; }
    public void setName(String name)       { this.name = name; }

    @Override
    public String toString() { return name; }
}
