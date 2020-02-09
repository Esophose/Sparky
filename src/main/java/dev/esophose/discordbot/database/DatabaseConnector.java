package dev.esophose.discordbot.database;

import java.sql.Connection;
import java.sql.SQLException;

public interface DatabaseConnector {

    /**
     * Checks if the connection to the database has been created
     *
     * @return true if the connection is created, otherwise false
     */
    boolean isInitialized();

    /**
     * Closes all open connections to the database
     */
    void closeConnection();

    /**
     * Executes a callback with a Connection passed and automatically closes it when finished
     *
     * @param callback The callback to execute once the connection is retrieved
     */
    void connect(ConnectionCallback callback);

    /**
     * Executes a callback with a Connection passed and automatically closes it when finished
     *
     * @param callback The callback to execute once the connection is retrieved
     * @return The value returned by the callback
     */
    <T> T connect(Class<T> returnType, ConnectionCallbackReturning<T> callback);

    /**
     * Wraps a connection in a callback which will automagically handle catching sql errors
     */
    @FunctionalInterface
    interface ConnectionCallback {
        void accept(Connection connection) throws SQLException;
    }

    /**
     * Wraps a connection in a callback which will automagically handle catching sql errors
     * Returns a value based on the connection
     */
    @FunctionalInterface
    interface ConnectionCallbackReturning<T> {
        T accept(Connection connection) throws SQLException;
    }

}