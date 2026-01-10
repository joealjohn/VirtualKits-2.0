package dev.ace.virtualkit.storage;

import dev.ace.virtualkit.storage.exceptions.StorageConnectionException;
import dev.ace.virtualkit.storage.exceptions.StorageOperationException;
import dev.ace.virtualkit.storage.sql.SQLDatabase;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * SQLite-specific storage implementation.
 * Uses INSERT OR REPLACE syntax which is SQLite's proper upsert mechanism.
 */
public class SQLiteStorage implements StorageManager {

    private final SQLDatabase db;

    public SQLiteStorage(SQLDatabase db) {
        this.db = db;
    }

    private void createTable() throws SQLException {
        try (Connection conn = db.getConnection();
                PreparedStatement ps = conn.prepareStatement(
                        "CREATE TABLE IF NOT EXISTS kits (KITID TEXT PRIMARY KEY, KITDATA TEXT)")) {
            ps.executeUpdate();
        }
    }

    @Override
    public void init() throws StorageOperationException {
        try {
            createTable();
        } catch (SQLException e) {
            throw new StorageOperationException("Failed to initialize SQLite database", e);
        }
    }

    @Override
    public void connect() throws StorageConnectionException {
        try {
            db.connect();
        } catch (ClassNotFoundException | SQLException e) {
            throw new StorageConnectionException("Failed to connect to SQLite database", e);
        }
    }

    @Override
    public boolean isConnected() {
        return db.isConnected();
    }

    @Override
    public void close() throws StorageConnectionException {
        try {
            db.disconnect();
        } catch (SQLException e) {
            throw new StorageConnectionException("Failed to close SQLite connection", e);
        }
    }

    @Override
    public void keepAlive() throws StorageConnectionException {
        try (Connection conn = db.getConnection();
                PreparedStatement ps = conn.prepareStatement("SELECT 1")) {
            ps.executeQuery();
        } catch (SQLException e) {
            throw new StorageConnectionException("Failed to keep SQLite connection alive", e);
        }
    }

    @Override
    public void saveKitDataByID(String kitID, String data) {
        // SQLite uses INSERT OR REPLACE for upsert
        try (Connection conn = db.getConnection();
                PreparedStatement ps = conn.prepareStatement(
                        "INSERT OR REPLACE INTO kits (KITID, KITDATA) VALUES (?, ?)")) {
            ps.setString(1, kitID);
            ps.setString(2, data);
            int result = ps.executeUpdate();
            if (result <= 0) {
                System.err.println("[VirtualKit] Warning: No rows affected when saving kit: " + kitID);
            }
        } catch (SQLException e) {
            System.err.println("[VirtualKit] Error saving kit " + kitID + ": " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public String getKitDataByID(String kitID) {
        try (Connection conn = db.getConnection();
                PreparedStatement ps = conn.prepareStatement(
                        "SELECT KITDATA FROM kits WHERE KITID = ?")) {
            ps.setString(1, kitID);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("KITDATA");
                }
            }
        } catch (SQLException e) {
            System.err.println("[VirtualKit] Error loading kit " + kitID + ": " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public boolean doesKitExistByID(String kitID) {
        try (Connection conn = db.getConnection();
                PreparedStatement ps = conn.prepareStatement(
                        "SELECT 1 FROM kits WHERE KITID = ?")) {
            ps.setString(1, kitID);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            System.err.println("[VirtualKit] Error checking kit existence " + kitID + ": " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public void deleteKitByID(String kitID) {
        try (Connection conn = db.getConnection();
                PreparedStatement ps = conn.prepareStatement(
                        "DELETE FROM kits WHERE KITID = ?")) {
            ps.setString(1, kitID);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("[VirtualKit] Error deleting kit " + kitID + ": " + e.getMessage());
            e.printStackTrace();
        }
    }
}
