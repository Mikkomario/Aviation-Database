package vf.aviation.core.database.access.many.station

import utopia.vault.nosql.access.ManyRowModelAccess
import vf.aviation.core.database.factory.station.StationTypeFactory
import vf.aviation.core.model.stored.station.StationType

/**
 * Used for reading station types from the database
 * @author Mikko Hilpinen
 * @since 2.4.2021, v0.1
 */
object DbStationTypes extends ManyRowModelAccess[StationType]
{
	override def factory = StationTypeFactory
	
	override def globalCondition = None
}
