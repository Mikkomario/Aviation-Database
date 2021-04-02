package vf.aviation.core.database.access.single.country

import utopia.vault.nosql.access.SingleModelAccessById
import utopia.flow.generic.ValueConversions._
import vf.aviation.core.database.factory.country.WorldAreaFactory
import vf.aviation.core.model.stored.country.WorldArea

/**
 * Used for reading world area code information from the database
 * @author Mikko Hilpinen
 * @since 2.4.2021, v0.1
 */
object DbWorldArea extends SingleModelAccessById[WorldArea, Int]
{
	override def factory = WorldAreaFactory
	
	override def idToValue(id: Int) = id
}
