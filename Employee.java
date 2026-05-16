package ska.db;

import ska.model.Project;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for Project table operations.
 */
public class ProjectDAO {

    // ------------------------------------------------------------------
    // CREATE
    // ------------------------------------------------------------------

    public boolean addProject(Project p) {
        String sql = "INSERT INTO Project " +
                     "(name, sector, material, location_id, fee, " +
                     "owner_id, architect_id, principal_id, project_manager_id) " +
                     "VALUES (?,?,?,?,?,?,?,?,?)";
        try (PreparedStatement ps = DatabaseManager.getConnection().prepareStatement(sql)) {
            ps.setString(1, p.getProjectName());
            ps.setString(2, p.getSector());
            ps.setString(3, p.getMaterial());
            ps.setInt(4, p.getLocationId());
            ps.setDouble(5, p.getFee());
            ps.setInt(6, p.getOwnerId());
            ps.setInt(7, p.getArchitectId());
            ps.setInt(8, p.getPrincipalId());
            ps.setInt(9, p.getProjectManagerId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Insert project and return generated project_id, or -1 on failure.
     */
    public int addProjectReturnId(Project p) {
        String sql = "INSERT INTO Project " +
                     "(name, sector, material, location_id, fee, " +
                     "owner_id, architect_id, principal_id, project_manager_id) " +
                     "VALUES (?,?,?,?,?,?,?,?,?)";
        try (PreparedStatement ps = DatabaseManager.getConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, p.getProjectName());
            ps.setString(2, p.getSector());
            ps.setString(3, p.getMaterial());
            ps.setInt(4, p.getLocationId());
            ps.setDouble(5, p.getFee());
            ps.setInt(6, p.getOwnerId());
            ps.setInt(7, p.getArchitectId());
            ps.setInt(8, p.getPrincipalId());
            ps.setInt(9, p.getProjectManagerId());
            int affected = ps.executeUpdate();
            if (affected == 0) return -1;
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) return keys.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    // ------------------------------------------------------------------
    // READ — all projects
    // ------------------------------------------------------------------

    public List<Project> getAllProjects() {
        List<Project> list = new ArrayList<>();
        String sql = "SELECT p.*, l.state FROM Project p " +
                     "JOIN Location l ON p.location_id = l.location_id " +
                     "ORDER BY p.name";
        try (Statement st = DatabaseManager.getConnection().createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    // ------------------------------------------------------------------
    // READ — project by ID
    // ------------------------------------------------------------------

    public Project getProjectById(int projectId) {
        String sql = "SELECT p.*, l.state FROM Project p " +
                     "JOIN Location l ON p.location_id = l.location_id " +
                     "WHERE p.project_id = ?";
        try (PreparedStatement ps = DatabaseManager.getConnection().prepareStatement(sql)) {
            ps.setInt(1, projectId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return mapRow(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    // ------------------------------------------------------------------
    // READ — projects filtered by sector
    // ------------------------------------------------------------------

    public List<Project> getProjectsBySector(String sector) {
        List<Project> list = new ArrayList<>();
        String sql = "SELECT p.*, l.state FROM Project p " +
                     "JOIN Location l ON p.location_id = l.location_id " +
                     "WHERE p.sector = ? ORDER BY p.name";
        try (PreparedStatement ps = DatabaseManager.getConnection().prepareStatement(sql)) {
            ps.setString(1, sector);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    // ------------------------------------------------------------------
    // READ — projects in a given location
    // ------------------------------------------------------------------

    public List<Project> getProjectsByLocation(int locationId) {
        List<Project> list = new ArrayList<>();
        String sql = "SELECT p.*, l.state FROM Project p " +
                     "JOIN Location l ON p.location_id = l.location_id " +
                     "WHERE p.location_id = ? ORDER BY p.name";
        try (PreparedStatement ps = DatabaseManager.getConnection().prepareStatement(sql)) {
            ps.setInt(1, locationId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    // ------------------------------------------------------------------
    // READ — projects in a given state (by state name)
    // ------------------------------------------------------------------

    public List<Project> getProjectsByState(String state) {
        List<Project> list = new ArrayList<>();
        String sql = "SELECT p.*, l.state FROM Project p " +
                     "JOIN Location l ON p.location_id = l.location_id " +
                     "WHERE l.state = ? ORDER BY p.name";
        try (PreparedStatement ps = DatabaseManager.getConnection().prepareStatement(sql)) {
            ps.setString(1, state);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    // ------------------------------------------------------------------
    // READ — profitability for a project
    // Returns: fee - sum(man_hours * cost_rate) across all employees
    // ------------------------------------------------------------------

    public double calculateProfitability(int projectId) {
        // First get the fee
        double fee = 0;
        String feeSql = "SELECT fee FROM Project WHERE project_id = ?";
        try (PreparedStatement ps = DatabaseManager.getConnection().prepareStatement(feeSql)) {
            ps.setInt(1, projectId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) fee = rs.getDouble("fee");
        } catch (SQLException e) {
            e.printStackTrace();
            return 0;
        }

        // Then get total cost: sum of (man_hours * rank_cost) for all employees on project
        double totalCost = 0;
        String costSql = "SELECT SUM(pe.man_hours * r.rank_cost) AS total_cost " +
                         "FROM Project_Employee pe " +
                         "JOIN Employee e ON pe.employee_id = e.employee_id " +
                         "JOIN Employee_Rank r ON e.rank_id = r.rank_id " +
                         "WHERE pe.project_id = ?";
        try (PreparedStatement ps = DatabaseManager.getConnection().prepareStatement(costSql)) {
            ps.setInt(1, projectId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) totalCost = rs.getDouble("total_cost");
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return fee - totalCost;
    }

    // ------------------------------------------------------------------
    // UPDATE
    // ------------------------------------------------------------------

    public boolean updateProject(Project p) {
        String sql = "UPDATE Project SET name=?, sector=?, material=?, " +
                     "location_id=?, fee=?, owner_id=?, architect_id=?, " +
                     "principal_id=?, project_manager_id=? " +
                     "WHERE project_id=?";
        try (PreparedStatement ps = DatabaseManager.getConnection().prepareStatement(sql)) {
            ps.setString(1, p.getProjectName());
            ps.setString(2, p.getSector());
            ps.setString(3, p.getMaterial());
            ps.setInt(4, p.getLocationId());
            ps.setDouble(5, p.getFee());
            ps.setInt(6, p.getOwnerId());
            ps.setInt(7, p.getArchitectId());
            ps.setInt(8, p.getPrincipalId());
            ps.setInt(9, p.getProjectManagerId());
            ps.setInt(10, p.getProjectId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // ------------------------------------------------------------------
    // DELETE
    // ------------------------------------------------------------------

    public boolean deleteProject(int projectId) {
        Connection conn = null;
        try {
            conn = DatabaseManager.getConnection();
            conn.setAutoCommit(false);

            // Delete assignments first to satisfy FK constraints
            try (PreparedStatement ps = conn.prepareStatement("DELETE FROM Project_Employee WHERE project_id = ?")) {
                ps.setInt(1, projectId);
                ps.executeUpdate();
            }

            // Delete the project
            try (PreparedStatement ps = conn.prepareStatement("DELETE FROM Project WHERE project_id = ?")) {
                ps.setInt(1, projectId);
                int affected = ps.executeUpdate();
                conn.commit();
                return affected > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            try { if (conn != null) conn.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            return false;
        } finally {
            try { if (conn != null) conn.setAutoCommit(true); } catch (SQLException ignored) {}
        }
    }

    // ------------------------------------------------------------------
    // Helper — maps a ResultSet row to a Project object
    // ------------------------------------------------------------------

    private Project mapRow(ResultSet rs) throws SQLException {
        return new Project(
            rs.getInt("project_id"),
            rs.getString("name"),
            rs.getString("sector"),
            rs.getString("material"),
            rs.getDouble("fee"),
            rs.getInt("location_id"),
            rs.getString("state"),
            rs.getInt("owner_id"),
            rs.getInt("architect_id"),
            rs.getInt("principal_id"),
            rs.getInt("project_manager_id")
        );
    }
}