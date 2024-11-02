package com.github.hadywalied.zakker.infrastructure
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import java.sql.Connection
import java.sql.DriverManager

class DatabaseConfig {
    private var connection: Connection? = null

    fun getConnection(): Connection {
        // Disable SQLite logging
        System.setProperty("sqlite.debug", "false")

        // Load SQLite JDBC driver
        Class.forName("org.sqlite.JDBC")

        // Get the resource as stream
        val inputStream = javaClass.getResourceAsStream("/database/azkar-db")
            ?: throw IllegalStateException("Database resource not found")

        // Create a temporary file
        val tempFile = Files.createTempFile("azkar-db", ".sqlite")

        // Copy the database to the temporary file
        inputStream.use { input ->
            Files.copy(input, tempFile, StandardCopyOption.REPLACE_EXISTING)
        }

        // Connect to the temporary database file
        return DriverManager.getConnection("jdbc:sqlite:${tempFile.toAbsolutePath()}")
    }


    fun closeConnection() {
        connection?.close()
        connection = null
    }
}
