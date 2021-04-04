package vf.aviation.core.database.access.many.country

import utopia.vault.nosql.access.ManyRowModelAccess
import vf.aviation.core.database.factory.country.WorldAreaFactory
import vf.aviation.core.model.stored.country.WorldArea

/**
 * Used for accessing multiple world areas at a time
 * @author Mikko Hilpinen
 * @since 4.4.2021, v0.1
 */
object DbWorldAreas extends ManyRowModelAccess[WorldArea]
{
	override def factory = WorldAreaFactory
	
	override def globalCondition = None
}
