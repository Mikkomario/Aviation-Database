package vf.aviation.core.database

import utopia.vault.database

/**
 * Connection pool used in this project for opening, reusing and maintaining database connections
 * @author Mikko Hilpinen
 * @since 29.3.2021, v0.1
 */
object ConnectionPool extends database.ConnectionPool()
