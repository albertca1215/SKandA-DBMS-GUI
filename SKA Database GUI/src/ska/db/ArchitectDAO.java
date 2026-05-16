package ska.db;

import ska.model.Architect;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for Architect table operations.
 */
public class ArchitectDAO {

    // ------------------------------------------------------------------
    // CREATE
    // ------------------------------------------------------------------

    public boolean addArchitect(Architect architect) {
        String sql = "INSERT INTO Architect (name) VALUES (?)";
        try (PreparedStatement ps = DatabaseManager.getConnection().prepareStatement(sql)) {
            ps.setString(1, architect.getName());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // ------------------------------------------------------------------
    // READ — all architects
    // ------------------------------------------------------------------

    public List<Architect> getAllArchitects() {
        List<Architect> list = new ArrayList<>();
        String sql = "SELECT * FROM Architect ORDER BY name";
        try (Statement st = DatabaseManager.getConnection().createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                list.add(new Architect(
                    rs.getInt("architect_id"),
                    rs.getString("name")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    // ------------------------------------------------------------------
    // READ — architect by ID
    // ------------------------------------------------------------------

    // ------------------------------------------------------------------
    // READ — architect by name
    // ------------------------------------------------------------------

    public Architect getArchitectByName(String name) {
        String sql = "SELECT * FROM Architect WHERE name = ?";
        try (PreparedStatement ps = DatabaseManager.getConnection().prepareStatement(sql)) {
            ps.setString(1, name);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return new Architect(
                    rs.getInt("architect_id"),
                    rs.getString("name")
                );
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    // ------------------------------------------------------------------
    // READ — architect by ID
    // ------------------------------------------------------------------

    public Architect getArchitectById(int architectId) {
        String sql = "SELECT * FROM Architect WHERE architect_id = ?";
        try (PreparedStatement ps = DatabaseManager.getConnection().prepareStatement(sql)) {
            ps.setInt(1, architectId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return new Architect(
                    rs.getInt("architect_id"),
                    rs.getString("name")
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

    public boolean updateArchitect(Architect architect) {
        String sql = "UPDATE Architect SET name=? WHERE architect_id=?";
        try (PreparedStatement ps = DatabaseManager.getConnection().prepareStatement(sql)) {
            ps.setString(1, architect.getName());
            ps.setInt(2, architect.getArchitectId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // ------------------------------------------------------------------
    // DELETE
    // ------------------------------------------------------------------

    public boolean deleteArchitect(int architectId) {
        String sql = "DELETE FROM Architect WHERE architect_id = ?";
        try (PreparedStatement ps = DatabaseManager.getConnection().prepareStatement(sql)) {
            ps.setInt(1, architectId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}
