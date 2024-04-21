package seb.dal;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public enum DatabaseManager {
    INSTANCE;
    public Connection getConnection()
    {
        try {
            return DriverManager.getConnection(
                    "jdbc:postgresql://localhost:5432/seb_db",
                    "seb_user",
                    "seb_password"
            );
        } catch (SQLException e) {
            throw new DataAccessException("Datenbankverbindungsaufbau nicht erfolgreich", e);
        }
    }
}
