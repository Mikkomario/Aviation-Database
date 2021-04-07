package vf.aviation.core.model.stored.aircraft

import utopia.vault.model.template.Stored
import vf.aviation.core.model.partial.aircraft.AircraftModelVariantData

/**
 * Represents an aircraft model variant that has been registered in the DB
 * @author Mikko Hilpinen
 * @since 7.4.2021, v0.1
 */
case class AircraftModelVariant(id: Int, data: AircraftModelVariantData)
	extends Stored[AircraftModelVariantData, Int]
