package vf.aviation.core.database.access.single.aircraft

import utopia.vault.database.Connection
import utopia.vault.nosql.access.{IndexedAccess, SingleRowModelAccess, UniqueModelAccess}
import utopia.flow.generic.ValueConversions._
import utopia.vault.sql.Condition
import vf.aviation.core.database.factory.aircraft.{AircraftManufacturerFactory, FullAircraftManufacturerFactory}
import vf.aviation.core.database.model.aircraft.AircraftManufacturerModel
import vf.aviation.core.model.stored.aircraft.AircraftManufacturer

/**
 * Used for accessing individual aircraft manufacturers from the DB
 * @author Mikko Hilpinen
 * @since 5.4.2021, v0.1
 */
object DbAircraftManufacturer extends SingleRowModelAccess[AircraftManufacturer]
	with IndexedAccess[Option[AircraftManufacturer]]
{
	// COMPUTED ---------------------------------
	
	private def fullFactory = FullAircraftManufacturerFactory
	private def model = AircraftManufacturerModel
	
	
	// IMPLEMENTED  -----------------------------
	
	override def factory = AircraftManufacturerFactory
	
	override def globalCondition = None
	
	
	// OTHER    ---------------------------------
	
	/**
	 * @param manufacturerId Aircraft manufacturer id
	 * @return An access point to that aircraft manufacturer's data
	 */
	def apply(manufacturerId: Int) = new UniqueAircraftManufacturerAccess(index <=> manufacturerId)
	
	/**
	 * @param icaoCode Aircraft manufacturer ICAO code
	 * @return An access point to that aircraft manufacturer
	 */
	def withIcaoCode(icaoCode: String) = new UniqueAircraftManufacturerAccess(model.withIcaoCode(icaoCode).toCondition)
	
	
	// NESTED   ---------------------------------
	
	class UniqueAircraftManufacturerAccess(override val condition: Condition)
		extends UniqueModelAccess[AircraftManufacturer] with SingleRowModelAccess[AircraftManufacturer]
	{
		// COMPUTED -----------------------------
		
		/**
		 * @param connection DB Connection (implicit)
		 * @return This manufacturer, including name data
		 */
		def full(implicit connection: Connection) = fullFactory.get(condition)
		
		/**
		 * @param connection Implicit DB Connection
		 * @return Id of the country associated with this manufacturer
		 */
		def countryId(implicit connection: Connection) = pullAttribute(model.countryIdAttName).int
		/**
		 * Assigns a new country id for this manufacturer
		 * @param newCountryId Id of the associated country
		 * @param connection DB Connection (implicit)
		 * @return Whether a row was updated
		 */
		def countryId_=(newCountryId: Int)(implicit connection: Connection) =
			putAttribute(model.countryIdAttName, newCountryId)
		
		
		// IMPLEMENTED  -------------------------
		
		override def factory = DbAircraftManufacturer.factory
	}
}
