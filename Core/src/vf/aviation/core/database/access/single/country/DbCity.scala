package vf.aviation.core.database.access.single.country

import utopia.flow.util.StringExtensions._
import utopia.vault.database.Connection
import utopia.vault.nosql.access.SingleRowModelAccess
import vf.aviation.core.database.factory.country.CityFactory
import vf.aviation.core.database.model.country.CityModel
import vf.aviation.core.model.stored.country.City

/**
 * Used for accessing individual city data in DB
 * @author Mikko Hilpinen
 * @since 3.4.2021, v0.1
 */
object DbCity extends SingleRowModelAccess[City]
{
	// COMPUTED ------------------------------
	
	private def model = CityModel
	
	
	// IMPLEMENTED  --------------------------
	
	override def factory = CityFactory
	
	override def globalCondition = None
	
	
	// OTHER    ------------------------------
	
	/**
	 * @param countryId Id of the targeted country
	 * @return An access point to individual cities in that country
	 */
	def inCountryWithId(countryId: Int) = DbCityInCountry(countryId)
	
	private def cityNameCondition(name: String) = model.withName(name).toCondition
	
	
	// NESTED   ------------------------------
	
	case class DbCityInCountry(countryId: Int) extends SingleRowModelAccess[City]
	{
		// IMPLEMENTED  ----------------------
		
		override def factory = DbCity.factory
		
		override def globalCondition = Some(DbCity.mergeCondition(model.withCountryId(countryId)))
		
		
		// OTHER    --------------------------
		
		/**
		 * Finds a city in this country with name like the one specified
		 * @param cityName City name to search
		 * @param connection DB Connection (implicit)
		 * @return Matching city. None if no such city was found.
		 */
		def withName(cityName: String)(implicit connection: Connection) =
		{
			// Searches for direct name matches first and if that doesn't work, word containment
			find(cityNameCondition(cityName)).orElse {
				val nameColumn = model.nameColumn
				val wordConditions = cityName.words.map(nameColumn.contains)
				val wordCondition = wordConditions.head && wordConditions.tail
				factory.getMany(mergeCondition(wordCondition)).minByOption { _.name.length }
			}
		}
	}
}
