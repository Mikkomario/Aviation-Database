package vf.aviation.core.database.model.aircraft

import utopia.flow.generic.ValueConversions._
import utopia.vault.database.Connection
import utopia.vault.model.immutable.StorableWithFactory
import utopia.vault.sql.Insert
import vf.aviation.core.database.factory.aircraft.AircraftManufacturerNameFactory
import vf.aviation.core.model.partial.aircraft.AircraftManufacturerNameData
import vf.aviation.core.model.stored.aircraft.AircraftManufacturerName

object AircraftManufacturerNameModel
{
	// ATTRIBUTES   ----------------------
	
	/**
	 * Name of the attribute that refers to aircraft manufacturer
	 */
	val manufacturerIdAttName = "manufacturerId"
	/**
	 * Name of the attribute that contains manufacturer name
	 */
	val nameAttName = "name"
	
	
	// COMPUTED --------------------------
	
	/**
	 * @return Factory used by this model
	 */
	def factory = AircraftManufacturerNameFactory
	/**
	 * @return Table used by this model
	 */
	def table = factory.table
	
	/**
	 * @return Column that contains assigned name
	 */
	def nameColumn = table(nameAttName)
	
	
	// OTHER    --------------------------
	
	/**
	 * @param manufacturerId Aircraft manufacturer id
	 * @return A model with only manufacturer id set
	 */
	def withManufacturerId(manufacturerId: Int) = apply(manufacturerId = Some(manufacturerId))
	/**
	 * @param name Manufacturer name
	 * @return A model with only name set
	 */
	def withName(name: String) = apply(name = Some(name))
	
	/**
	 * @param data Aircraft manufacturer name link data
	 * @return A model matching that data
	 */
	def apply(data: AircraftManufacturerNameData): AircraftManufacturerNameModel =
		apply(None, Some(data.manufacturerId), Some(data.name))
	
	/**
	 * Inserts a new aircraft manufacturer name
	 * @param data Data to insert
	 * @param connection DB Connection (implicit)
	 * @return Generated link id
	 */
	def insert(data: AircraftManufacturerNameData)(implicit connection: Connection) =
		AircraftManufacturerName(apply(data).insert().getInt, data)
	
	/**
	 * Inserts new aircraft manufacturer names
	 * @param data Manufacturer id + name pairs to insert
	 * @param connection DB Connection (implicit)
	 * @return Generated link ids
	 */
	def insert(data: Seq[AircraftManufacturerNameData])(implicit connection: Connection) =
	{
		val ids = Insert(table, data.map { apply(_).toModel }).generatedIntKeys
		ids.zip(data).map { case (id, data) => AircraftManufacturerName(id, data) }
	}
}

/**
 * Used for interacting with aircraft manufacturer names in DB
 * @author Mikko Hilpinen
 * @since 4.4.2021, v0.1
 */
case class AircraftManufacturerNameModel(id: Option[Int] = None, manufacturerId: Option[Int] = None,
                                         name: Option[String] = None)
	extends StorableWithFactory[AircraftManufacturerName]
{
	import AircraftManufacturerNameModel._
	
	override def factory = AircraftManufacturerNameModel.factory
	
	override def valueProperties = Vector("id" -> id, manufacturerIdAttName -> manufacturerId, nameAttName -> name)
}
