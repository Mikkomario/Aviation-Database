package vf.aviation.core.database.model.aircraft

import utopia.flow.generic.ValueConversions._
import utopia.vault.database.Connection
import utopia.vault.model.immutable.StorableWithFactory
import utopia.vault.sql.Insert
import vf.aviation.core.database.factory.aircraft.AircraftManufacturerFactory
import vf.aviation.core.model.partial.aircraft.AircraftManufacturerData
import vf.aviation.core.model.stored.aircraft.AircraftManufacturer

object AircraftManufacturerModel
{
	// ATTRIBUTES   ----------------------
	
	/**
	 * Name of the attribute that contains associated country's id
	 */
	val countryIdAttName = "countryId"
	
	
	// COMPUTED --------------------------
	
	/**
	 * @return Factory used by this model
	 */
	def factory = AircraftManufacturerFactory
	/**
	 * @return Table used by this model
	 */
	def table = factory.table
	
	
	// OTHER    --------------------------
	
	/**
	 * @param icaoCode Manufacturer icao code
	 * @return A model with that code
	 */
	def withIcaoCode(icaoCode: String) = apply(icaoCode = Some(icaoCode))
	
	/**
	 * @param data Aircraft manufacturer data
	 * @return A model matching that data
	 */
	def apply(data: AircraftManufacturerData): AircraftManufacturerModel =
		apply(None, data.icaoCode, data.alternativeCode, data.countryId)
	
	/**
	 * Inserts a new aircraft manufacturer to DB
	 * @param data Data to insert
	 * @param connection DB Connection (implicit)
	 * @return Inserted manufacturer
	 */
	def insert(data: AircraftManufacturerData)(implicit connection: Connection) =
		AircraftManufacturer(apply(data).insert().getInt, data)
	
	/**
	 * Inserts multiple new aircraft manufacturers to DB
	 * @param data Data to insert
	 * @param connection DB Connection (implicit)
	 * @return Inserted manufacturers
	 */
	def insert(data: Seq[AircraftManufacturerData])(implicit connection: Connection) =
	{
		val ids = Insert(table, data.map { apply(_).toModel }).generatedIntKeys
		ids.zip(data).map { case (id, data) => AircraftManufacturer(id, data) }
	}
}

/**
 * Used for interacting with aircraft manufacturers in the DB
 * @author Mikko Hilpinen
 * @since 4.4.2021, v0.1
 */
case class AircraftManufacturerModel(id: Option[Int] = None, icaoCode: Option[String] = None,
                                     altCode: Option[String] = None, countryId: Option[Int] = None)
	extends StorableWithFactory[AircraftManufacturer]
{
	import AircraftManufacturerModel._
	
	override def factory = AircraftManufacturerModel.factory
	
	override def valueProperties = Vector("id" -> id, "icaoCode" -> icaoCode, "altCode" -> altCode,
		countryIdAttName -> countryId)
}
