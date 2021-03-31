package vf.aviation.core.database

import utopia.vault.database.Tables
import utopia.vault.model.immutable.Table
import vf.aviation.core.util.Globals._

/**
 * Used for accessing database tables in this project
 * @author Mikko Hilpinen
 * @since 29.3.2021, v0.1
 */
object AviationTables extends Tables(ConnectionPool)
{
	/**
	 * @return Table that records all world regions
	 */
	def worldRegion = apply("world_region")
	/**
	 * @return Table that records countries
	 */
	def country = apply("country")
	/**
	 * @return Table that lists recorded country states
	 */
	def state = apply("state")
	/**
	 * @return Table that contains world areas
	 */
	def worldArea = apply("world_area")
	/**
	 * @return Table that records cities
	 */
	def city = apply("city")
	
	private def apply(tableName: String): Table = apply("aviation_database", tableName)
}
