package vf.aviation.core.model.combined

import utopia.flow.util.Extender
import vf.aviation.core.model.partial.aircraft.AircraftManufacturerData
import vf.aviation.core.model.stored.aircraft.AircraftManufacturer

/**
 * An aircraft manufacturer model which also contains all the names
 * @author Mikko Hilpinen
 * @since 5.4.2021, v0.1
 */
case class FullAircraftManufacturer(manufacturer: AircraftManufacturer, names: Vector[String])
	extends Extender[AircraftManufacturerData]
{
	// COMPUTED ----------------------------
	
	/**
	 * @return Unique id of this manufacturer
	 */
	def id = manufacturer.id
	
	
	// IMPLEMENTED  ------------------------
	
	override def wrapped = manufacturer.data
}
