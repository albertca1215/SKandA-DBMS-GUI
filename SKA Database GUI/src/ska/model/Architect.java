package ska.model;

/**
 * Represents an Architect who contracts with SK&A.
 */
public class Architect {

    private int    architectId;
    private String name;

    public Architect() {}

    public Architect(int architectId, String name) {
        this.architectId = architectId;
        this.name = name;
    }

    // Getters
    public int    getArchitectId() { return architectId; }
    public String getName()        { return name; }

    // Setters
    public void setArchitectId(int architectId) { this.architectId = architectId; }
    public void setName(String name)            { this.name = name; }

    @Override
    public String toString() { return name; }
}
