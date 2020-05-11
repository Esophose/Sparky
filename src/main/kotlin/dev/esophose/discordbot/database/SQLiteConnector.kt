package dev.esophose.discordbot.database

import java.io.File
import java.sql.Connection
import java.sql.DriverManager
import java.sql.SQLException

class SQLiteConnector(directory: File, dbName: String) : DatabaseConnector {

    private val connectionString = "jdbc:sqlite:" + File(directory, "$dbName.db").absolutePath
    private var connection: Connection? = null

    init {
        Class.forName("org.sqlite.JDBC")
    }

    override fun closeConnection() {
        try {
            if (this.connection != null) {
                this.connection!!.close()
            }
        } catch (ex: SQLException) {
            System.err.println("An error occurred closing the SQLite database connection: " + ex.message)
        }

    }

    override fun connect(callback: (Connection) -> Unit) {
        if (this.connection == null) {
            try {
                this.connection = DriverManager.getConnection(this.connectionString)
            } catch (ex: SQLException) {
                System.err.println("An error occurred retrieving the SQLite database connection: " + ex.message)
            }
        }

        try {
            callback(this.connection!!)
        } catch (ex: Exception) {
            System.err.println("An error occurred executing an SQLite query: " + ex.message)
            ex.printStackTrace()
        }
    }

}
