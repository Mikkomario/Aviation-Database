package vf.aviation.core.database.model.aircraft

import utopia.flow.generic.ValueConversions._
import utopia.vault.database.Connection
import utopia.vault.model.immutable.Storable
import utopia.vault.sql.Insert
import vf.aviation.core.database.AviationTables

object AircraftManufacturerNameModel
{
	// ATTRIBUTES   ----------------------
	
	/**
	 * Name of the attribute that contains manufacturer name
	 */
	val nameAttName = "name"
	
	
	// COMPUTED --------------------------
	
	/**
	 * @return Table used by this model
	 */
	def table = AviationTables.aircraftManufacturerName
	
	
	// OTHER    --------------------------
	
	/**
	 * Inserts a new aircraft manufacturer name
	 * @param manufacturerId Id of the targeted manufacturer
	 * @param name Name assigned for the manufacturer
	 * @param connection DB Connection (implicit)
	 * @return Generated link id
	 */
	def insert(manufacturerId: Int, name: String)(implicit connection: Connection) =
		apply(None, Some(manufacturerId), Some(name)).insert().getInt
	
	/**
	 * Inserts new aircraft manufacturer names
	 * @param data Manufacturer id + name pairs to insert
	 * @param connection DB Connection (implicit)
	 * @return Generated link ids
	 */
	def insert(data: Seq[(Int, String)])(implicit connection: Connection) =
		Insert(table, data.map { case (manufacturerId, name) =>
			apply(None, Some(manufacturerId), Some(name)).toModel }).generatedIntKeys
}

/**
 * Used for interacting with aircraft manufacturer names in DB
 * @author Mikko Hilpinen
 * @since 4.4.2021, v0.1
 */
case class AircraftManufacturerNameModel(id: Option[Int] = None, manufacturerId: Option[Int] = None,
                                         name: Option[String] = None) extends Storable
{
	import AircraftManufacturerNameModel._
	
	override def table = AircraftManufacturerModel.table
	
	override def valueProperties = Vector("id" -> id, "manufacturerId" -> manufacturerId, nameAttName -> name)
}
