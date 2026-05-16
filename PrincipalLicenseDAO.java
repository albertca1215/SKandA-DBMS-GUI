package ska.db;

import ska.model.Employee;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for Employee table operations.
 * Add new query methods here as needed.
 */
public class EmployeeDAO {

    // ------------------------------------------------------------------
    // CREATE
    // ------------------------------------------------------------------

    public boolean addEmployee(Employee emp) {
        String sql = "INSERT INTO Employee (first_name, last_name, rank_id) VALUES (?, ?, ?)";
        try (PreparedStatement ps = DatabaseManager.getConnection().prepareStatement(sql)) {
            ps.setString(1, emp.getFirstName());
            ps.setString(2, emp.getLastName());
            ps.setInt(3, emp.getRankId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // ------------------------------------------------------------------
    // READ — all employees
    // ------------------------------------------------------------------

    public List<Employee> getAllEmployees() {
        List<Employee> list = new ArrayList<>();
        // Joins Employee with Employee_Rank to get rank name and rank cost
        String sql = "SELECT e.employee_id, e.first_name, e.last_name, " +
                     "e.rank_id, r.rank_name, r.rank_cost " +
                     "FROM Employee e " +
                     "JOIN Employee_Rank r ON e.rank_id = r.rank_id " +
                     "ORDER BY e.last_name";
        try (Statement st = DatabaseManager.getConnection().createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                list.add(new Employee(
                    rs.getInt("employee_id"),
                    rs.getString("first_name"),
                    rs.getString("last_name"),
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
    // READ — employees filtered by work history (sector, material, or both)
    // Pass null to skip that filter.
    // ------------------------------------------------------------------

    public List<Employee> getEmployeesByWorkHistory(String sector, String material) {
        List<Employee> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder(
            "SELECT DISTINCT e.employee_id, e.first_name, e.last_name, " +
            "e.rank_id, r.rank_name, r.rank_cost " +
            "FROM Employee e " +
            "JOIN Employee_Rank r ON e.rank_id = r.rank_id " +
            "JOIN Project_Employee pe ON e.employee_id = pe.employee_id " +
            "JOIN Project p ON pe.project_id = p.project_id " +
            "WHERE 1=1 "
        );
        List<String> params = new ArrayList<>();
        if (sector != null && !sector.isEmpty()) {
            sql.append("AND p.sector = ? ");
            params.add(sector);
        }
        if (material != null && !material.isEmpty()) {
            sql.append("AND p.material = ? ");
            params.add(material);
        }
        sql.append("ORDER BY e.last_name");

        try (PreparedStatement ps = DatabaseManager.getConnection().prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                ps.setString(i + 1, params.get(i));
            }
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(new Employee(
                    rs.getInt("employee_id"),
                    rs.getString("first_name"),
                    rs.getString("last_name"),
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
    // READ — principals licensed in a given state
    // Explicitly filters for Principal rank only
    // ------------------------------------------------------------------

    public List<Employee> getPrincipalsByState(String state) {
        List<Employee> list = new ArrayList<>();
        String sql = "SELECT e.employee_id, e.first_name, e.last_name, " +
                     "e.rank_id, r.rank_name, r.rank_cost " +
                     "FROM Employee e " +
                     "JOIN Employee_Rank r ON e.rank_id = r.rank_id " +
                     "JOIN Principal_License pl ON e.employee_id = pl.employee_id " +
                     "JOIN Location l ON pl.location_id = l.location_id " +
                     "WHERE l.state = ? AND r.rank_name = 'Principal' " +
                     "ORDER BY e.last_name";
        try (PreparedStatement ps = DatabaseManager.getConnection().prepareStatement(sql)) {
            ps.setString(1, state);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(new Employee(
                    rs.getInt("employee_id"),
                    rs.getString("first_name"),
                    rs.getString("last_name"),
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
    // READ — employees filtered by rank name
    // Generic method for rank-based filtering
    // ------------------------------------------------------------------

    public List<Employee> getEmployeesByRank(String rankName) {
        List<Employee> list = new ArrayList<>();
        String sql = "SELECT e.employee_id, e.first_name, e.last_name, " +
                     "e.rank_id, r.rank_name, r.rank_cost " +
                     "FROM Employee e " +
                     "JOIN Employee_Rank r ON e.rank_id = r.rank_id " +
                     "WHERE r.rank_name = ? " +
                     "ORDER BY e.last_name";
        try (PreparedStatement ps = DatabaseManager.getConnection().prepareStatement(sql)) {
            ps.setString(1, rankName);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(new Employee(
                    rs.getInt("employee_id"),
                    rs.getString("first_name"),
                    rs.getString("last_name"),
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
    // READ — all principals
    // Returns employees with rank = 'Principal'
    // ------------------------------------------------------------------

    public List<Employee> getPrincipals() {
        return getEmployeesByRank("Principal");
    }

    // ------------------------------------------------------------------
    // READ — all project managers
    // Returns employees with rank = 'Associate', 'Project Manager', or 'Assistant Project Manager'
    // ------------------------------------------------------------------

    public List<Employee> getProjectManagers() {
        List<Employee> list = new ArrayList<>();
        String sql = "SELECT e.employee_id, e.first_name, e.last_name, " +
                     "e.rank_id, r.rank_name, r.rank_cost " +
                     "FROM Employee e " +
                     "JOIN Employee_Rank r ON e.rank_id = r.rank_id " +
                     "WHERE r.rank_name IN ('Associate', 'Project Manager', 'Assistant Project Manager') " +
                     "ORDER BY e.last_name";
        try (Statement st = DatabaseManager.getConnection().createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                list.add(new Employee(
                    rs.getInt("employee_id"),
                    rs.getString("first_name"),
                    rs.getString("last_name"),
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
    // READ — employee by ID
    // ------------------------------------------------------------------

    public Employee getEmployeeById(int employeeId) {
        String sql = "SELECT e.employee_id, e.first_name, e.last_name, " +
                     "e.rank_id, r.rank_name, r.rank_cost " +
                     "FROM Employee e " +
                     "JOIN Employee_Rank r ON e.rank_id = r.rank_id " +
                     "WHERE e.employee_id = ?";
        try (PreparedStatement ps = DatabaseManager.getConnection().prepareStatement(sql)) {
            ps.setInt(1, employeeId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return new Employee(
                    rs.getInt("employee_id"),
                    rs.getString("first_name"),
                    rs.getString("last_name"),
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

    public boolean updateEmployee(Employee emp) {
        String sql = "UPDATE Employee SET first_name=?, last_name=?, rank_id=? " +
                     "WHERE employee_id=?";
        try (PreparedStatement ps = DatabaseManager.getConnection().prepareStatement(sql)) {
            ps.setString(1, emp.getFirstName());
            ps.setString(2, emp.getLastName());
            ps.setInt(3, emp.getRankId());
            ps.setInt(4, emp.getEmployeeId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // ------------------------------------------------------------------
    // DELETE
    // ------------------------------------------------------------------

    public boolean deleteEmployee(int employeeId) {
        Connection conn = null;
        try {
            conn = DatabaseManager.getConnection();
            conn.setAutoCommit(false);

            // Delete dependent Principal_License rows
            try (PreparedStatement ps = conn.prepareStatement("DELETE FROM Principal_License WHERE employee_id = ?")) {
                ps.setInt(1, employeeId);
                ps.executeUpdate();
            }

            // Delete dependent Project_Employee rows
            try (PreparedStatement ps = conn.prepareStatement("DELETE FROM Project_Employee WHERE employee_id = ?")) {
                ps.setInt(1, employeeId);
                ps.executeUpdate();
            }

            // Now delete the employee
            try (PreparedStatement ps = conn.prepareStatement("DELETE FROM Employee WHERE employee_id = ?")) {
                ps.setInt(1, employeeId);
                int affected = ps.executeUpdate();
                conn.commit();
                return affected > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            try {
                if (conn != null) conn.rollback();
            } catch (SQLException ex) { ex.printStackTrace(); }
            return false;
        } finally {
            try {
                if (conn != null) conn.setAutoCommit(true);
            } catch (SQLException ignored) {}
        }
    }
}
