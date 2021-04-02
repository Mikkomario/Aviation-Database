package vf.aviation.core.model.stored

import utopia.vault.model.template.Stored
import vf.aviation.core.model.partial.StationData

/**
 * Represents an airport or another station that has been recorded to the database
 * @author Mikko Hilpinen
 * @since 2.4.2021, v0.1
 */
case class Station(id: Int, data: StationData) extends Stored[StationData, Int]
