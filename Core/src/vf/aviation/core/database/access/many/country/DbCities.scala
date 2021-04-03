package vf.aviation.core.database.access.many.country

import utopia.flow.generic.ValueConversions._
import utopia.vault.database.Connection
import utopia.vault.nosql.access.ManyRowModelAccess
import utopia.vault.sql.Extensions._
import vf.aviation.core.database.factory.country.CityFactory
import vf.aviation.core.database.model.country.CityModel
import vf.aviation.core.model.stored.country.City

/**
 * Used for reading city data from the DB
 * @author Mikko Hilpinen
 * @since 2.4.2021, v0.1
 */
object DbCities extends ManyRowModelAccess[City]
{
	// COMPUTED --------------------------
	
	private def model = CityModel
	
	
	// IMPLEMENTED  ----------------------
	
	override def factory = CityFactory
	
	override def globalCondition = None
	
	
	// OTHER    --------------------------
	
	/**
	 * @param cityIds City ids
	 * @return An access point to cities with those ids
	 */
	def withIds(cityIds: Set[Int]) = DbCitiesWithIds(cityIds)
	
	/**
	 * Finds cities with the specified name (case-insensitive) from the database.
	 * Will not use word containment search. Please note that this query is relatively slow and should not
	 * be preferred.
	 * @param cityName Targeted city name
	 * @param connection DB Connection (implicit)
	 * @return Cities with that name
	 */
	def withExactName(cityName: String)(implicit connection: Connection) =
		find(model.withName(cityName).toCondition)
	
	
	// NESTED   --------------------------
	
	case class DbCitiesWithIds(cityIds: Set[Int]) extends ManyRowModelAccess[City]
	{
		// COMPUTED -------------------------
		
		/**
		 * @param connection Implicit DB Connection
		 * @return Ids of the countries which are linked with these cities
		 */
		def countryIds(implicit connection: Connection) =
			pullAttribute(model.countryIdAttName).flatMap { _.int }.toSet
		
		
		// IMPLEMENTED  ---------------------
		
		override def factory = DbCities.factory
		
		override def globalCondition = Some(table.primaryColumn.get.in(cityIds))
	}
}
