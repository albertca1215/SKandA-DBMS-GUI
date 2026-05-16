package ska.db;

import java.sql.*;

/**
 * Manages the MySQL connection for the SK&A database.
 * Edit the URL, USER, and PASSWORD constants to match your MySQL setup.
 */
public class DatabaseManager {

    // ---------------------------------------------------------------
    // !!! EDIT THESE to match your local MySQL configuration !!!
    private static final String URL      = "jdbc:mysql://triton2.towson.edu:3360/acantor1db?serverTimezone=EST";
    private static final String USER     = "acantor1";
    private static final String PASSWORD = "COSC*vxj5f";
    // ---------------------------------------------------------------

    private static Connection connection = null;

    /** Returns a shared connection, creating it if necessary. */
    public static Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            try {
                Class.forName("com.mysql.cj.jdbc.Driver");
                connection = DriverManager.getConnection(URL, USER, PASSWORD);
                System.out.println("Database connected successfully.");
            } catch (ClassNotFoundException e) {
                throw new SQLException("MySQL JDBC Driver not found. Add the JAR to your project libraries.", e);
            }
        }
        return connection;
    }

    /** Call this when the application closes. */
    public static void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                System.out.println("Database connection closed.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /** Quick connectivity test — prints success or error to console. */
    public static boolean testConnection() {
        try {
            getConnection();
            return true;
        } catch (SQLException e) {
            System.err.println("Connection failed: " + e.getMessage());
            return false;
        }
    }
}