package vf.aviation.core.database.access.many.country

import utopia.flow.generic.ValueConversions._
import utopia.vault.nosql.access.{ManyModelAccessById, ManyRowModelAccess}
import vf.aviation.core.database.factory.country.CountryFactory
import vf.aviation.core.model.stored.country.Country

/**
 * Used for reading country data from the dabtabase
 * @author Mikko Hilpinen
 * @since 2.4.2021, v0.1
 */
object DbCountries extends ManyRowModelAccess[Country] with ManyModelAccessById[Country, Int]
{
	override def factory = CountryFactory
	
	override def idToValue(id: Int) = id
}
