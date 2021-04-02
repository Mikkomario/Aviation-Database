package vf.aviation.core.database.access.id.many

import utopia.flow.datastructure.immutable.Value
import utopia.vault.nosql.access.ManyIdAccess
import vf.aviation.core.database.AviationTables

/**
 * Used for accessing multiple daylight saving zone codes at a time
 * @author Mikko Hilpinen
 * @since 2.4.2021, v0.1
 */
object DbDaylightSavingZoneCodes extends ManyIdAccess[Char]
{
	// IMPLEMENTED  --------------------------
	
	override def table = AviationTables.daylightSavingZone
	
	override def target = table
	
	override def globalCondition = None
	
	override def valueToId(value: Value) = value.string.flatMap { _.headOption }
}
