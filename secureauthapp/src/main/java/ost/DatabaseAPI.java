package ost;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

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
                var sql = "CREATE TABLE IF NOT EXISTS " + tableName + "(\n " + fields + ");";
                var stmt = conn.createStatement();
                stmt.executeUpdate(sql);
                stmt.close();
                System.out.println("Table " + tableName + " has been created.");
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public void insert(String tableName, String fields, String values) {
        try (Connection conn = DriverManager.getConnection(url)) {
            if (conn != null) {            
                var stmt = conn.createStatement();
                var sql = "INSERT INTO " + tableName + "(" + fields + ") VALUES (" + values + ")";
                stmt.executeUpdate(sql);
                stmt.close();
                System.out.println("Insert in " + tableName + " is done");
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        } 
    }

    public String getValue(String tableName, String keyName, String keyValue, String fieldName) {
        try (Connection conn = DriverManager.getConnection(url)) {
            if (conn != null) {            
                var stmt = conn.createStatement();
                var sql = "SELECT * FROM " + tableName + " WHERE " + keyName + " = '" + keyValue + "'";
                var rs = stmt.executeQuery(sql);
                if (rs.next()) {
                    return rs.getString(fieldName);
                }
                stmt.close();
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return null;
    }

    public boolean isKeyAvailable(String tableName, String keyName, String keyValue) {
        try (Connection conn = DriverManager.getConnection(url)) {
            if (conn != null) {  
                var stmt = conn.createStatement();
                var sql = "SELECT * FROM " + tableName + " WHERE " + keyName + " = '" + keyValue + "'"; 
                var rs = stmt.executeQuery(sql);
                return rs.next();
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return false;
    }
}