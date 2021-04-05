package vf.aviation.core.model.stored.aircraft

import utopia.vault.model.template.Stored
import vf.aviation.core.model.partial.aircraft.AircraftManufacturerNameData

/**
 * Represets a stored aircraft manufacturer-name link
 * @author Mikko Hilpinen
 * @since 5.4.2021, v0.1
 */
case class AircraftManufacturerName(id: Int, data: AircraftManufacturerNameData)
	extends Stored[AircraftManufacturerNameData, Int]
