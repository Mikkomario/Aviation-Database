package vf.aviation.core.database.access.many.aircraft

import utopia.flow.generic.ValueConversions._
import utopia.vault.database.Connection
import utopia.vault.nosql.access.{Indexed, ManyRowModelAccess}
import utopia.vault.sql.Extensions._
import vf.aviation.core.database.factory.aircraft.AircraftManufacturerFactory
import vf.aviation.core.database.model.aircraft.AircraftManufacturerModel
import vf.aviation.core.model.stored.aircraft.AircraftManufacturer

/**
 * Used for accessing a number of aircraft manufacturers at a time
 * @author Mikko Hilpinen
 * @since 6.4.2021, v0.1
 */
object DbAircraftManufacturers extends ManyRowModelAccess[AircraftManufacturer] with Indexed
{
	// COMPUTED -----------------------------
	
	private def model = AircraftManufacturerModel
	
	
	// IMPLEMENTED  -------------------------
	
	override def factory = AircraftManufacturerFactory
	
	override def globalCondition = None
	
	
	// OTHER    -----------------------------
	
	/**
	 * @param ids Manufacturer ids
	 * @return An access point to manufacturers with those ids
	 */
	def apply(ids: Iterable[Int]) = DbAircraftManufacturersByIds(ids)
	
	
	// NESTED   -----------------------------
	
	case class DbAircraftManufacturersByIds(ids: Iterable[Int]) extends ManyRowModelAccess[AircraftManufacturer]
	{
		// COMPUTED -------------------------
		
		/**
		 * @param connection DB Connection (implicit)
		 * @return Alternative 3 character codes assigned for these manufacturers where / if defined
		 */
		def alternativeCodes(implicit connection: Connection) =
			pullAttribute(model.altCodeAttName).flatMap { _.string }
		/**
		 * Sets the same 3 character code for all of these manufacturers
		 * @param newCode Code to assign for these manufacturers
		 * @param connection DB Connection (implicit)
		 * @return Whether any row was modified
		 */
		def alternativeCodes_=(newCode: String)(implicit connection: Connection) =
			putAttribute(model.altCodeAttName, newCode)
		
		
		// IMPLEMENTED  ---------------------
		
		override def factory = DbAircraftManufacturers.factory
		
		override def globalCondition = Some(index.in(ids))
		
		
		// OTHER    -------------------------
		
		/**
		 * Assigns An alternative 3-character code for these manufacturers, but ignores manufacturers that
		 * already have a code assigned
		 * @param newCode Code to assign
		 * @param connection DB Connection (implicit)
		 * @return Whether any row was updated
		 */
		def assignAlternativeCodeIfNotSet(newCode: String)(implicit connection: Connection) =
			putAttribute(model.altCodeAttName, newCode, Some(model.altCodeColumn.isNull))
	}
}
