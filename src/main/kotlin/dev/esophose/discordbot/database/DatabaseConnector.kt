package dev.esophose.discordbot.database

import java.sql.Connection

interface DatabaseConnector {

    /**
     * Closes all open connections to the database
     */
    fun closeConnection()

    /**
     * Executes a callback with a Connection passed and automatically closes it when finished
     *
     * @param callback The callback to execute once the connection is retrieved
     */
    fun connect(callback: (Connection) -> Unit)

}
