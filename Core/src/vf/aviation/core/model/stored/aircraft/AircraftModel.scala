package vf.aviation.core.model.stored.aircraft

import utopia.vault.model.template.Stored
import vf.aviation.core.model.partial.aircraft.AircraftModelData

/**
 * Represents an aircraft model that has been stored to the DB
 * @author Mikko Hilpinen
 * @since 7.4.2021, v0.1
 */
case class AircraftModel(id: Int, data: AircraftModelData) extends Stored[AircraftModelData, Int]
