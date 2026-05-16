package ska.db;

import ska.model.Owner;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for Owner table operations.
 */
public class OwnerDAO {

    // ------------------------------------------------------------------
    // CREATE
    // ------------------------------------------------------------------

    public boolean addOwner(Owner owner) {
        String sql = "INSERT INTO Owner (name) VALUES (?)";
        try (PreparedStatement ps = DatabaseManager.getConnection().prepareStatement(sql)) {
            ps.setString(1, owner.getName());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // ------------------------------------------------------------------
    // READ — all owners
    // ------------------------------------------------------------------

    public List<Owner> getAllOwners() {
        List<Owner> list = new ArrayList<>();
        String sql = "SELECT * FROM Owner ORDER BY name";
        try (Statement st = DatabaseManager.getConnection().createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                list.add(new Owner(
                    rs.getInt("owner_id"),
                    rs.getString("name")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    // ------------------------------------------------------------------
    // READ — owner by ID
    // ------------------------------------------------------------------

    // ------------------------------------------------------------------
    // READ — owner by name
    // ------------------------------------------------------------------

    public Owner getOwnerByName(String name) {
        String sql = "SELECT * FROM Owner WHERE name = ?";
        try (PreparedStatement ps = DatabaseManager.getConnection().prepareStatement(sql)) {
            ps.setString(1, name);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return new Owner(
                    rs.getInt("owner_id"),
                    rs.getString("name")
                );
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    // ------------------------------------------------------------------
    // READ — owner by ID
    // ------------------------------------------------------------------

    public Owner getOwnerById(int ownerId) {
        String sql = "SELECT * FROM Owner WHERE owner_id = ?";
        try (PreparedStatement ps = DatabaseManager.getConnection().prepareStatement(sql)) {
            ps.setInt(1, ownerId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return new Owner(
                    rs.getInt("owner_id"),
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

    public boolean updateOwner(Owner owner) {
        String sql = "UPDATE Owner SET name=? WHERE owner_id=?";
        try (PreparedStatement ps = DatabaseManager.getConnection().prepareStatement(sql)) {
            ps.setString(1, owner.getName());
            ps.setInt(2, owner.getOwnerId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // ------------------------------------------------------------------
    // DELETE
    // ------------------------------------------------------------------

    public boolean deleteOwner(int ownerId) {
        String sql = "DELETE FROM Owner WHERE owner_id = ?";
        try (PreparedStatement ps = DatabaseManager.getConnection().prepareStatement(sql)) {
            ps.setInt(1, ownerId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}
