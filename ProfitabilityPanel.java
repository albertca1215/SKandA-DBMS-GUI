package ska.model;

/**
 * Represents an employee rank in SK&A.
 * Ranks from lowest to highest:
 * - Project Engineer 1-3
 * - Assistant Project Manager
 * - Project Manager
 * - Associate
 * - Principal
 */
public class Rank {

    private int    rankId;
    private String rankName;
    private double costRate;  // Hourly cost for this rank

    public Rank() {}

    public Rank(int rankId, String rankName, double costRate) {
        this.rankId = rankId;
        this.rankName = rankName;
        this.costRate = costRate;
    }

    // Getters
    public int    getRankId()   { return rankId; }
    public String getRankName() { return rankName; }
    public double getCostRate() { return costRate; }

    // Setters
    public void setRankId(int rankId)       { this.rankId = rankId; }
    public void setRankName(String rankName) { this.rankName = rankName; }
    public void setCostRate(double costRate) { this.costRate = costRate; }

    @Override
    public String toString() { return rankName + " ($" + String.format("%.2f", costRate) + "/hr)"; }
}
