package dev.esophose.discordbot.database;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class SQLiteConnector implements DatabaseConnector {

    private final String connectionString;
    private Connection connection;

    public SQLiteConnector(File directory, String dbName) {
        this.connectionString = "jdbc:sqlite:" + new File(directory, dbName + ".db").getAbsolutePath();

        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean isInitialized() {
        return true; // Always available
    }

    @Override
    public void closeConnection() {
        try {
            if (this.connection != null) {
                this.connection.close();
            }
        } catch (SQLException ex) {
            System.err.println("An error occurred closing the SQLite database connection: " + ex.getMessage());
        }
    }

    @Override
    public void connect(ConnectionCallback callback) {
        if (this.connection == null) {
            try {
                this.connection = DriverManager.getConnection(this.connectionString);
            } catch (SQLException ex) {
                System.err.println("An error occurred retrieving the SQLite database connection: " + ex.getMessage());
            }
        }

        try {
            callback.accept(this.connection);
        } catch (Exception ex) {
            System.err.println("An error occurred executing an SQLite query: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    @Override
    public <T> T connect(Class<T> returnType, ConnectionCallbackReturning<T> callback) {
        if (this.connection == null) {
            try {
                this.connection = DriverManager.getConnection(this.connectionString);
            } catch (SQLException ex) {
                System.err.println("An error occurred retrieving the SQLite database connection: " + ex.getMessage());
            }
        }

        try {
            return callback.accept(this.connection);
        } catch (Exception ex) {
            System.err.println("An error occurred executing an SQLite query: " + ex.getMessage());
            ex.printStackTrace();
        }

        return null;
    }

}
