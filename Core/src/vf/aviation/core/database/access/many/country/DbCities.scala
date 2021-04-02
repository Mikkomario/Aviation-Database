package vf.aviation.core.database.access.many.country

import utopia.vault.nosql.access.ManyRowModelAccess
import vf.aviation.core.database.factory.country.CityFactory
import vf.aviation.core.model.stored.country.City

/**
 * Used for reading city data from the DB
 * @author Mikko Hilpinen
 * @since 2.4.2021, v0.1
 */
object DbCities extends ManyRowModelAccess[City]
{
	override def factory = CityFactory
	
	override def globalCondition = None
}
