package vf.aviation.core.database.access.single.aircraft

import utopia.vault.nosql.access.SingleRowModelAccess
import vf.aviation.core.database.factory.aircraft.AircraftManufacturerNameFactory
import vf.aviation.core.model.stored.aircraft.AircraftManufacturerName

/**
 * Used for accessing individual aircraft manufacturer names
 * @author Mikko Hilpinen
 * @since 5.4.2021, v0.1
 */
object DbAircraftManufacturerName extends SingleRowModelAccess[AircraftManufacturerName]
{
	// IMPLEMENTED  --------------------------
	
	override def factory = AircraftManufacturerNameFactory
	
	override def globalCondition = None
}
