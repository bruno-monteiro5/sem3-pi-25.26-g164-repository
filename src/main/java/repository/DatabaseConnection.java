package repository;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

// DatabaseConnection.java - MODIFICAR
public class DatabaseConnection {
    private static final String URL = "jdbc:oracle:thin:@vsgate-s1.dei.isep.ipp.pt:10796:XE";
    private static final String USER = "C##LAPR3_G164";
    private static final String PASSWORD = "oracle";

    private static Connection connection = null;
    private static boolean VERBOSE = true; //

    public static Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            try {
                Class.forName("oracle.jdbc.driver.OracleDriver");
                connection = DriverManager.getConnection(URL, USER, PASSWORD);

                if (VERBOSE) { // ← Só mostra se ativo
                    System.out.println("✓ Database connection established");
                }
            } catch (ClassNotFoundException e) {
                throw new SQLException("Oracle JDBC Driver not found", e);
            }
        }
        return connection;
    }

    public static void closeConnection() {
        if (connection != null) {
            try {
                connection.close();
                if (VERBOSE) {
                    System.out.println("✓ Database connection closed");
                }
            } catch (SQLException e) {
                System.err.println("Error closing connection: " + e.getMessage());
            }
        }
    }

    public static void setVerbose(boolean verbose) {
        VERBOSE = verbose;
    }
}