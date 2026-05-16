package ska.db;

import ska.model.PrincipalLicense;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for Principal_License table operations.
 * Manages which principals are licensed in which states.
 */
public class PrincipalLicenseDAO {

    // ------------------------------------------------------------------
    // CREATE
    // ------------------------------------------------------------------

    public boolean addPrincipalLicense(PrincipalLicense pl) {
        String sql = "INSERT INTO Principal_License (employee_id, location_id) VALUES (?, ?)";
        try (PreparedStatement ps = DatabaseManager.getConnection().prepareStatement(sql)) {
            ps.setInt(1, pl.getEmployeeId());
            ps.setInt(2, pl.getLocationId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // ------------------------------------------------------------------
    // READ — all licenses
    // ------------------------------------------------------------------

    public List<PrincipalLicense> getAllLicenses() {
        List<PrincipalLicense> list = new ArrayList<>();
        String sql = "SELECT * FROM Principal_License";
        try (Statement st = DatabaseManager.getConnection().createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                list.add(new PrincipalLicense(
                    rs.getInt("employee_id"),
                    rs.getInt("location_id")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    // ------------------------------------------------------------------
    // READ — all licenses for a specific principal
    // ------------------------------------------------------------------

    public List<PrincipalLicense> getLicensesByPrincipal(int employeeId) {
        List<PrincipalLicense> list = new ArrayList<>();
        String sql = "SELECT * FROM Principal_License WHERE employee_id = ?";
        try (PreparedStatement ps = DatabaseManager.getConnection().prepareStatement(sql)) {
            ps.setInt(1, employeeId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(new PrincipalLicense(
                    rs.getInt("employee_id"),
                    rs.getInt("location_id")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    // ------------------------------------------------------------------
    // READ — all licenses for a specific location/state
    // ------------------------------------------------------------------

    public List<PrincipalLicense> getLicensesByLocation(int locationId) {
        List<PrincipalLicense> list = new ArrayList<>();
        String sql = "SELECT * FROM Principal_License WHERE location_id = ?";
        try (PreparedStatement ps = DatabaseManager.getConnection().prepareStatement(sql)) {
            ps.setInt(1, locationId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(new PrincipalLicense(
                    rs.getInt("employee_id"),
                    rs.getInt("location_id")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    // ------------------------------------------------------------------
    // READ — check if a principal is licensed in a specific location
    // ------------------------------------------------------------------

    public boolean isPrincipalLicensedIn(int employeeId, int locationId) {
        String sql = "SELECT 1 FROM Principal_License WHERE employee_id = ? AND location_id = ?";
        try (PreparedStatement ps = DatabaseManager.getConnection().prepareStatement(sql)) {
            ps.setInt(1, employeeId);
            ps.setInt(2, locationId);
            ResultSet rs = ps.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // ------------------------------------------------------------------
    // UPDATE
    // ------------------------------------------------------------------

    public boolean updatePrincipalLicense(PrincipalLicense pl) {
        // Cannot update composite key — delete and re-add instead
        // For simplicity, just ensure the license exists
        return addPrincipalLicense(pl);
    }

    // ------------------------------------------------------------------
    // DELETE
    // ------------------------------------------------------------------

    public boolean deletePrincipalLicense(int employeeId, int locationId) {
        String sql = "DELETE FROM Principal_License WHERE employee_id = ? AND location_id = ?";
        try (PreparedStatement ps = DatabaseManager.getConnection().prepareStatement(sql)) {
            ps.setInt(1, employeeId);
            ps.setInt(2, locationId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}
