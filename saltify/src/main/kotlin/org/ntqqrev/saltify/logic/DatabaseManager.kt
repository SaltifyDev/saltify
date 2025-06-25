package org.ntqqrev.saltify.logic

import org.ktorm.database.Database
import org.ntqqrev.saltify.SaltifyApp

class DatabaseManager(val app: SaltifyApp) {
    private var databaseInstance: Database? = null
    val database: Database
        get() = databaseInstance ?: throw IllegalStateException("Database is not initialized!")

    fun initDatabase() {
        databaseInstance = Database.connect("jdbc:sqlite:${app.dbPath.toAbsolutePath()}")
    }
}