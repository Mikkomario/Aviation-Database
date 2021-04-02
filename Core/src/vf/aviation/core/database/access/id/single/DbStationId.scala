package vf.aviation.core.database.access.id.single

import utopia.vault.nosql.access.SingleIntIdAccess
import vf.aviation.core.database.factory.station.StationFactory

/**
 * Used for accessing individual station ids
 * @author Mikko Hilpinen
 * @since 2.4.2021, v0.1
 */
object DbStationId extends SingleIntIdAccess
{
	// COMPUTED ------------------------------
	
	private def factory = StationFactory
	
	
	// IMPLEMENTED  --------------------------
	
	override def target = table
	
	override def table = factory.table
	
	override def globalCondition = None
}
