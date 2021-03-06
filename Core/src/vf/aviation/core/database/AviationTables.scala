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
	 * @return Table that lists known daylight saving zones
	 */
	def daylightSavingZone = apply("daylight_saving_zone")
	
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
	
	/**
	 * @return Table that contains station type enumeration values
	 */
	def stationType = apply("station_type")
	/**
	 * @return Table that records airports and other stations
	 */
	def station = apply("station")
	
	/**
	 * @return Table that lists known ailines / carriers
	 */
	def carrier = apply("carrier")
	
	/**
	 * @return Table that lists known aircraft manufacturers
	 */
	def aircraftManufacturer = apply("aircraft_manufacturer")
	/**
	 * @return Table that contains various names assigned to aircraft manufacturers
	 */
	def aircraftManufacturerName = apply("aircraft_manufacturer_name")
	/**
	 * @return Table that lists general A/C models (codes)
	 */
	def aircraftModel = apply("aircraft_model")
	/**
	 * @return Table that lists specific A/C model variatns
	 */
	def aircraftModelVariant = apply("aircraft_model_variant")
	
	private def apply(tableName: String): Table = apply("aviation_database", tableName)
}
