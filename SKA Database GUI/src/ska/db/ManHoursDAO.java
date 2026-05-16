package ska.db;

import ska.model.ManHours;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for Project_Employee (ManHours) table operations.
 * Manages the many-to-many relationship between Projects and Employees.
 * Each record represents an employee working on a project with a specified number of man-hours.
 */
public class ManHoursDAO {

    // ------------------------------------------------------------------
    // CREATE — add employee to project with man-hours
    // ------------------------------------------------------------------

    public boolean addManHours(ManHours mh) {
        String sql = "INSERT INTO Project_Employee (project_id, employee_id, man_hours, role_on_project) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = DatabaseManager.getConnection().prepareStatement(sql)) {
            ps.setInt(1, mh.getProjectId());
            ps.setInt(2, mh.getEmployeeId());
            ps.setDouble(3, mh.getManHours());
            ps.setString(4, mh.getRoleOnProject());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // ------------------------------------------------------------------
    // READ — all man-hours records
    // ------------------------------------------------------------------

    public List<ManHours> getAllManHours() {
        List<ManHours> list = new ArrayList<>();
        String sql = "SELECT * FROM Project_Employee";
        try (Statement st = DatabaseManager.getConnection().createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                list.add(new ManHours(
                    rs.getInt("project_employee_id"),
                    rs.getInt("project_id"),
                    rs.getInt("employee_id"),
                    rs.getDouble("man_hours"),
                    rs.getString("role_on_project")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    /**
     * Check whether an assignment already exists for given project and employee.
     */
    public boolean assignmentExists(int projectId, int employeeId) {
        String sql = "SELECT COUNT(*) AS cnt FROM Project_Employee WHERE project_id = ? AND employee_id = ?";
        try (PreparedStatement ps = DatabaseManager.getConnection().prepareStatement(sql)) {
            ps.setInt(1, projectId);
            ps.setInt(2, employeeId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt("cnt") > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // ------------------------------------------------------------------
    // READ — all man-hours for a specific project
    // ------------------------------------------------------------------

    public List<ManHours> getManHoursByProject(int projectId) {
        List<ManHours> list = new ArrayList<>();
        String sql = "SELECT * FROM Project_Employee WHERE project_id = ?";
        try (PreparedStatement ps = DatabaseManager.getConnection().prepareStatement(sql)) {
            ps.setInt(1, projectId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(new ManHours(
                    rs.getInt("project_employee_id"),
                    rs.getInt("project_id"),
                    rs.getInt("employee_id"),
                    rs.getDouble("man_hours"),
                    rs.getString("role_on_project")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    // ------------------------------------------------------------------
    // READ — all man-hours for a specific employee
    // ------------------------------------------------------------------

    public List<ManHours> getManHoursByEmployee(int employeeId) {
        List<ManHours> list = new ArrayList<>();
        String sql = "SELECT * FROM Project_Employee WHERE employee_id = ?";
        try (PreparedStatement ps = DatabaseManager.getConnection().prepareStatement(sql)) {
            ps.setInt(1, employeeId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(new ManHours(
                    rs.getInt("project_employee_id"),
                    rs.getInt("project_id"),
                    rs.getInt("employee_id"),
                    rs.getDouble("man_hours"),
                    rs.getString("role_on_project")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    // ------------------------------------------------------------------
    // READ — man-hours for a specific employee on a specific project
    // ------------------------------------------------------------------

    public ManHours getManHours(int projectId, int employeeId) {
        String sql = "SELECT * FROM Project_Employee WHERE project_id = ? AND employee_id = ?";
        try (PreparedStatement ps = DatabaseManager.getConnection().prepareStatement(sql)) {
            ps.setInt(1, projectId);
            ps.setInt(2, employeeId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return new ManHours(
                    rs.getInt("project_employee_id"),
                    rs.getInt("project_id"),
                    rs.getInt("employee_id"),
                    rs.getDouble("man_hours"),
                    rs.getString("role_on_project")
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

    public boolean updateManHours(ManHours mh) {
        String sql = "UPDATE Project_Employee SET project_id=?, employee_id=?, man_hours=?, role_on_project=? WHERE project_employee_id=?";
        try (PreparedStatement ps = DatabaseManager.getConnection().prepareStatement(sql)) {
            ps.setInt(1, mh.getProjectId());
            ps.setInt(2, mh.getEmployeeId());
            ps.setDouble(3, mh.getManHours());
            ps.setString(4, mh.getRoleOnProject());
            ps.setInt(5, mh.getProjectEmployeeId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // ------------------------------------------------------------------
    // DELETE
    // ------------------------------------------------------------------

    public boolean deleteManHours(int projectEmployeeId) {
        String sql = "DELETE FROM Project_Employee WHERE project_employee_id = ?";
        try (PreparedStatement ps = DatabaseManager.getConnection().prepareStatement(sql)) {
            ps.setInt(1, projectEmployeeId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // ------------------------------------------------------------------
    // DELETE — remove employee from a specific project
    // ------------------------------------------------------------------

    public boolean deleteEmployeeFromProject(int projectId, int employeeId) {
        String sql = "DELETE FROM Project_Employee WHERE project_id = ? AND employee_id = ?";
        try (PreparedStatement ps = DatabaseManager.getConnection().prepareStatement(sql)) {
            ps.setInt(1, projectId);
            ps.setInt(2, employeeId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // ------------------------------------------------------------------
    // DELETE — remove a specific role assignment for an employee on a project
    // ------------------------------------------------------------------

    public boolean deleteEmployeeRoleFromProject(int projectId, int employeeId, String role) {
        String sql = "DELETE FROM Project_Employee WHERE project_id = ? AND employee_id = ? AND role_on_project = ?";
        try (PreparedStatement ps = DatabaseManager.getConnection().prepareStatement(sql)) {
            ps.setInt(1, projectId);
            ps.setInt(2, employeeId);
            ps.setString(3, role);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}
