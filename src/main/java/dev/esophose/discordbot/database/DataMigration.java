package dev.esophose.discordbot.database;

import java.sql.Connection;
import java.sql.SQLException;

public abstract class DataMigration {

    private final int revision;

    public DataMigration(int revision) {
        this.revision = revision;
    }

    /**
     * Migrates the database to this migration stage
     *
     * @param connector The connector for the database
     * @param connection The connection to the database
     * @throws SQLException Any error that occurs during the SQL execution
     */
    public abstract void migrate(DatabaseConnector connector, Connection connection) throws SQLException;

    /**
     * @return the revision number of this migration
     */
    public int getRevision() {
        return this.revision;
    }

}