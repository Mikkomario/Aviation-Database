package vf.aviation.core.model.stored.aircraft

import utopia.vault.model.template.Stored
import vf.aviation.core.model.partial.aircraft.AircraftManufacturerData

/**
 * Represents an aircraft manufacturer that has been stored to the DB
 * @author Mikko Hilpinen
 * @since 4.4.2021, v0.1
 */
case class AircraftManufacturer(id: Int, data: AircraftManufacturerData)
	extends Stored[AircraftManufacturerData, Int]
