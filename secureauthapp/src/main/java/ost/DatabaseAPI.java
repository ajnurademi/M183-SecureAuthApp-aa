package ost;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class DatabaseAPI {

    protected final String url;

    public DatabaseAPI() {
        String dirPath = System.getProperty("user.dir") + "/data";
        File dir = new File(dirPath);
        if (!dir.exists()) {
            dir.mkdirs();
            System.out.println("Verzeichnis 'data' wurde erstellt.");
        }

        url = "jdbc:sqlite:" + dirPath + "/db.sqlite";
    }

    public void createTable(String tableName, String fields) {
        try (var conn = DriverManager.getConnection(url)) {
            if (conn != null) {
                var sql = "CREATE TABLE IF NOT EXISTS " + tableName + " (" + fields + ");";
                try (var stmt = conn.createStatement()) {
                    stmt.execute(sql);
                }
            }
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
    }

    public void insert(String tableName, String fields, String values) {
        try (Connection conn = DriverManager.getConnection(url)) {
            var sql = "INSERT INTO " + tableName + "(" + fields + ") VALUES (" + values + ");";
            var stmt = conn.createStatement();
            stmt.executeUpdate(sql);
            stmt.close();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public String getValue(String tableName, String searchField, String searchValue, String resultField) {
        String result = null;
        try (Connection conn = DriverManager.getConnection(url)) {
            // Prepare the SQL query with the provided parameters
            String sql = "SELECT " + resultField + " FROM " + tableName + " WHERE " + searchField + " = ?";
            
            // Use PreparedStatement to prevent SQL injection
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, searchValue);
                try (ResultSet rs = pstmt.executeQuery()) {  // Execute the query and obtain a ResultSet
                    if (rs.next()) {  // Check if there's a result
                        result = rs.getString(resultField);  // Get the result
                    }
                }
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());  // Handle any SQL exceptions
        }
        return result;  // Return the result, null if no result found
    }
    

    public void update(String tableName, String updates, String condition) {
        try (Connection conn = DriverManager.getConnection(url)) {
            var sql = "UPDATE " + tableName + " SET " + updates + " WHERE " + condition + ";";
            var stmt = conn.createStatement();
            stmt.executeUpdate(sql);
            stmt.close();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }
}
