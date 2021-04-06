package vf.aviation.core.database.access.single.aircraft

import utopia.vault.database.Connection
import utopia.vault.nosql.access.{Indexed, SingleRowModelAccess, UniqueModelAccess}
import utopia.flow.generic.ValueConversions._
import utopia.vault.model.immutable.Column
import utopia.vault.sql.{Condition, Select, Where}
import vf.aviation.core.database.factory.aircraft.{AircraftManufacturerFactory, FullAircraftManufacturerFactory}
import vf.aviation.core.database.model.aircraft.{AircraftManufacturerModel, AircraftManufacturerNameModel}
import vf.aviation.core.model.stored.aircraft.{AircraftManufacturer, AircraftManufacturerName}
import vf.aviation.core.database.SqlExtensions._
import vf.aviation.core.database.access.many.aircraft.DbAircraftManufacturerNames

/**
 * Used for accessing individual aircraft manufacturers from the DB
 * @author Mikko Hilpinen
 * @since 5.4.2021, v0.1
 */
object DbAircraftManufacturer extends SingleRowModelAccess[AircraftManufacturer] with Indexed
{
	// COMPUTED ---------------------------------
	
	private def fullFactory = FullAircraftManufacturerFactory
	private def model = AircraftManufacturerModel
	
	private def nameModel = AircraftManufacturerNameModel
	
	
	// IMPLEMENTED  -----------------------------
	
	override def factory = AircraftManufacturerFactory
	
	override def globalCondition = None
	
	
	// OTHER    ---------------------------------
	
	/**
	 * @param manufacturerId Aircraft manufacturer id
	 * @return An access point to that aircraft manufacturer's data
	 */
	def apply(manufacturerId: Int) = DbSingleAircraftManufacturerById(manufacturerId)
	
	/**
	 * @param icaoCode Aircraft manufacturer ICAO code
	 * @return An access point to that aircraft manufacturer
	 */
	def withIcaoCode(icaoCode: String) = new UniqueAircraftManufacturerAccess(model.withIcaoCode(icaoCode).toCondition)
	
	/**
	 * Finds an aircraft manufacturer that best matches the specified name
	 * @param manufacturerName Manufacturer name
	 * @param connection DB Connection (implicit)
	 * @return Manufacturer that best matches the specified name
	 */
	def forName(manufacturerName: String)(implicit connection: Connection) =
	{
		// Searches for exact matches first (prefers icao code matches in searches)
		// 1: Exact icao match
		withIcaoCode(manufacturerName).pull
			// 2: Exact name match
			.orElse { nameReferenceSearch { _.matching(manufacturerName) } }
			// 3: Icao containment
			.orElse { icaoSearch { _.contains(manufacturerName) } }
			// 4: Name containment
			.orElse { nameReferenceSearch { _.containing(manufacturerName) } }
			// 5: Icao resemblance
			.orElse { icaoSearch { _.resembling(manufacturerName) } }
			// 6: Name resemblance
			.orElse { nameReferenceSearch { _.resembling(manufacturerName) } }
			// 7: Loose icao resemblance
			.orElse { icaoSearch { _.looselyResembling(manufacturerName) } }
			// 8: Loose name resemblance
			.orElse { nameReferenceSearch { _.looselyResembling(manufacturerName) } }
	}
	
	private def icaoSearch(f: Column => Condition)(implicit connection: Connection) =
		factory.getMany(f(model.icaoCodeColumn)).filter { _.icaoCode.isDefined }.minByOption { _.icaoCode.get.length }
	
	private def nameReferenceSearch(f: DbAircraftManufacturerNames.type => Iterable[AircraftManufacturerName])
	                               (implicit connection: Connection) =
		f(DbAircraftManufacturerNames).minByOption { _.name.length }.flatMap { n => apply(n.manufacturerId).pull }
	
	
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
		
		/**
		 * @param connection DB Connection (implicit)
		 * @return Alternative 3 character code for this manufacturer, if applicable & known
		 */
		def altCode(implicit connection: Connection) = pullAttribute(model.altCodeAttName).string
		/**
		 * Updates this manufacturer's alternative code
		 * @param newAltCode New alternative manufacturer code
		 * @param connection DB Connection (implicit)
		 * @return Whether a row was updated
		 */
		def altCode_=(newAltCode: String)(implicit connection: Connection) =
			putAttribute(model.altCodeAttName, newAltCode)
		
		
		// IMPLEMENTED  -------------------------
		
		override def factory = DbAircraftManufacturer.factory
	}
	
	case class DbSingleAircraftManufacturerById(id: Int) extends UniqueAircraftManufacturerAccess(index <=> id)
	{
		/**
		 * @param connection DB Connection (implicit)
		 * @return Names assigned for this manufacturer
		 */
		def names(implicit connection: Connection) =
			connection(Select(nameModel.table, nameModel.nameAttName) +
				Where(nameModel.withManufacturerId(id))).rowValues.flatMap { _.string }
	}
}
