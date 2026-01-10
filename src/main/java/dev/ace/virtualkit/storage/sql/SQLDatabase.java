package dev.ace.virtualkit.storage.sql;

import java.sql.Connection;
import java.sql.SQLException;

public interface SQLDatabase {

    boolean isConnected();

    void connect() throws ClassNotFoundException, SQLException;

    void disconnect() throws SQLException;

    Connection getConnection() throws SQLException;

}

