package ska.db;

import ska.model.Rank;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for Rank table operations.
 * Manages employee ranks and their associated cost rates.
 */
public class RankDAO {

    // ------------------------------------------------------------------
    // CREATE
    // ------------------------------------------------------------------

    public boolean addRank(Rank rank) {
        String sql = "INSERT INTO Employee_Rank (rank_name, rank_cost) VALUES (?, ?)";
        try (PreparedStatement ps = DatabaseManager.getConnection().prepareStatement(sql)) {
            ps.setString(1, rank.getRankName());
            ps.setDouble(2, rank.getCostRate());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // ------------------------------------------------------------------
    // READ — all ranks
    // ------------------------------------------------------------------

    public List<Rank> getAllRanks() {
        List<Rank> list = new ArrayList<>();
        String sql = "SELECT * FROM Employee_Rank ORDER BY rank_cost ASC";
        try (Statement st = DatabaseManager.getConnection().createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                list.add(new Rank(
                    rs.getInt("rank_id"),
                    rs.getString("rank_name"),
                    rs.getDouble("rank_cost")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    // ------------------------------------------------------------------
    // READ — rank by ID
    // ------------------------------------------------------------------

    public Rank getRankById(int rankId) {
        String sql = "SELECT * FROM Employee_Rank WHERE rank_id = ?";
        try (PreparedStatement ps = DatabaseManager.getConnection().prepareStatement(sql)) {
            ps.setInt(1, rankId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return new Rank(
                    rs.getInt("rank_id"),
                    rs.getString("rank_name"),
                    rs.getDouble("rank_cost")
                );
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    // ------------------------------------------------------------------
    // READ — rank by name
    // ------------------------------------------------------------------

    public Rank getRankByName(String rankName) {
        String sql = "SELECT * FROM Employee_Rank WHERE rank_name = ?";
        try (PreparedStatement ps = DatabaseManager.getConnection().prepareStatement(sql)) {
            ps.setString(1, rankName);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return new Rank(
                    rs.getInt("rank_id"),
                    rs.getString("rank_name"),
                    rs.getDouble("rank_cost")
                );
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    // ------------------------------------------------------------------
    // UPDATE
    // ------------------------------------------------------------------

    public boolean updateRank(Rank rank) {
        String sql = "UPDATE Employee_Rank SET rank_name=?, rank_cost=? WHERE rank_id=?";
        try (PreparedStatement ps = DatabaseManager.getConnection().prepareStatement(sql)) {
            ps.setString(1, rank.getRankName());
            ps.setDouble(2, rank.getCostRate());
            ps.setInt(3, rank.getRankId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // ------------------------------------------------------------------
    // DELETE
    // ------------------------------------------------------------------

    public boolean deleteRank(int rankId) {
        String sql = "DELETE FROM Employee_Rank WHERE rank_id = ?";
        try (PreparedStatement ps = DatabaseManager.getConnection().prepareStatement(sql)) {
            ps.setInt(1, rankId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}
