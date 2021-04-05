package vf.aviation.core.database.access.many.aircraft

import utopia.vault.database.Connection
import utopia.vault.nosql.access.ManyRowModelAccess
import vf.aviation.core.database.factory.aircraft.AircraftManufacturerNameFactory
import vf.aviation.core.database.model.aircraft.AircraftManufacturerNameModel
import vf.aviation.core.model.stored.aircraft.AircraftManufacturerName
import vf.aviation.core.database.SqlExtensions._

/**
 * Used for accessing multiple aircraft manufacturer name assignments at a time
 * @author Mikko Hilpinen
 * @since 5.4.2021, v0.1
 */
object DbAircraftManufacturerNames extends ManyRowModelAccess[AircraftManufacturerName]
{
	// COMPUTED ---------------------------------
	
	private def model = AircraftManufacturerNameModel
	
	
	// IMPLEMENTED  -----------------------------
	
	override def factory = AircraftManufacturerNameFactory
	
	override def globalCondition = None
	
	
	// OTHER    --------------------------------
	
	/**
	 * @param manufacturerId Id of an aircraft manufacturer
	 * @param connection DB Connection (implicit)
	 * @return All names assigned for that manufacturer
	 */
	def forManufacturerWithId(manufacturerId: Int)(implicit connection: Connection) =
		find(model.withManufacturerId(manufacturerId).toCondition)
	
	/**
	 * Finds assignments with exact specified name (case-insensitive)
	 * @param name Aircraft manufacturer name
	 * @param connection DB Connection (implicit)
	 * @return Assignments where that exact name is given
	 */
	def matching(name: String)(implicit connection: Connection) =
		find(model.withName(name).toCondition)
	
	/**
	 * @param name Aircraft manufacturer name
	 * @param connection Implicit db connection
	 * @return Name assignments that contain the specified name
	 */
	def containing(name: String)(implicit connection: Connection) =
		find(model.nameColumn.contains(name))
	
	/**
	 * @param name Aircraft manufacturer name
	 * @param connection Implicit db connection
	 * @return Name assignments that resemble the specified name
	 */
	def resembling(name: String)(implicit connection: Connection) =
		find(model.nameColumn.resembling(name))
	
	/**
	 * @param name Aircraft manufacturer name
	 * @param connection Implicit db connection
	 * @return Name assignments that loosely resemble the specified name
	 */
	def looselyResembling(name: String)(implicit connection: Connection) =
		find(model.nameColumn.looselyResembling(name))
}
