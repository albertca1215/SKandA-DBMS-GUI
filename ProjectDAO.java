package ska.db;

import ska.model.Location;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for Location (State) table operations.
 * Manages U.S. states and DC for principal licensing.
 */
public class LocationDAO {

    // ------------------------------------------------------------------
    // CREATE
    // ------------------------------------------------------------------

    public boolean addLocation(Location loc) {
        String sql = "INSERT INTO Location (state) VALUES (?)";
        try (PreparedStatement ps = DatabaseManager.getConnection().prepareStatement(sql)) {
            ps.setString(1, loc.getState());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // ------------------------------------------------------------------
    // READ — all locations
    // ------------------------------------------------------------------

    public List<Location> getAllLocations() {
        List<Location> list = new ArrayList<>();
        String sql = "SELECT * FROM Location ORDER BY state";
        try (Statement st = DatabaseManager.getConnection().createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                list.add(new Location(
                    rs.getInt("location_id"),
                    rs.getString("state")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    // ------------------------------------------------------------------
    // READ — location by ID
    // ------------------------------------------------------------------

    public Location getLocationById(int locationId) {
        String sql = "SELECT * FROM Location WHERE location_id = ?";
        try (PreparedStatement ps = DatabaseManager.getConnection().prepareStatement(sql)) {
            ps.setInt(1, locationId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return new Location(
                    rs.getInt("location_id"),
                    rs.getString("state")
                );
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    // ------------------------------------------------------------------
    // READ — location by state name
    // ------------------------------------------------------------------

    public Location getLocationByState(String state) {
        String sql = "SELECT * FROM Location WHERE state = ?";
        try (PreparedStatement ps = DatabaseManager.getConnection().prepareStatement(sql)) {
            ps.setString(1, state);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return new Location(
                    rs.getInt("location_id"),
                    rs.getString("state")
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

    public boolean updateLocation(Location loc) {
        String sql = "UPDATE Location SET state=? WHERE location_id=?";
        try (PreparedStatement ps = DatabaseManager.getConnection().prepareStatement(sql)) {
            ps.setString(1, loc.getState());
            ps.setInt(2, loc.getLocationId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // ------------------------------------------------------------------
    // DELETE
    // ------------------------------------------------------------------

    public boolean deleteLocation(int locationId) {
        String sql = "DELETE FROM Location WHERE location_id = ?";
        try (PreparedStatement ps = DatabaseManager.getConnection().prepareStatement(sql)) {
            ps.setInt(1, locationId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}
