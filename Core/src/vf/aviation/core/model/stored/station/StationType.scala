package vf.aviation.core.model.stored.station

import utopia.vault.model.template.Stored
import vf.aviation.core.model.partial.station.StationTypeData

/**
 * Represents a station type stored in the DB
 * @author Mikko Hilpinen
 * @since 2.4.2021, v0.1
 */
case class StationType(id: Int, data: StationTypeData) extends Stored[StationTypeData, Int]
