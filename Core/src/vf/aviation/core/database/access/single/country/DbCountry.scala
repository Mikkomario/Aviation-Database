package vf.aviation.core.database.access.single.country

import utopia.flow.generic.ValueConversions._
import utopia.flow.util.StringExtensions._
import utopia.vault.database.Connection
import utopia.vault.nosql.access.{SingleIdModelAccess, SingleRowModelAccess}
import utopia.vault.sql.{Select, Where}
import vf.aviation.core.database.factory.country.{CityFactory, CountryFactory}
import vf.aviation.core.database.model.country.CountryModel
import vf.aviation.core.model.stored.country.Country

/**
 * Used for accessing country data in the DB
 * @author Mikko Hilpinen
 * @since 31.3.2021, v0.1
 */
object DbCountry extends SingleRowModelAccess[Country]
{
	// COMPUTED -------------------------------
	
	private def model = CountryModel
	
	private def cityFactory = CityFactory
	private def cityTable = cityFactory.table
	
	
	// IMPLEMENTED  ---------------------------
	
	override def factory = CountryFactory
	
	override def globalCondition = None
	
	
	// OTHER    -------------------------------
	
	/**
	 * @param countryId Country id
	 * @return An access point to that country
	 */
	def apply(countryId: Int) = DbCountryById(countryId)
	
	/**
	 * @param isoCode Country ISO-code
	 * @param connection DB Connection (implicit)
	 * @return Country with a matching ISO-code
	 */
	def withIsoCode(isoCode: String)(implicit connection: Connection) =
		find(model.withIsoCode(isoCode).toCondition)
	
	/**
	 * Searches for a country with a name like the one presented (case-insensitive).
	 * @param countryName Country name
	 * @param ignoreCountriesWithIsoCode Whether countries with ISO-code should be ignored in this search
	 *                                   (true when ISO-code -based search has already been performed)
	 *                                   (default = false)
	 * @param connection DB Connection (implicit)
	 * @return Country that best matches that name. None if no such country could be found.
	 */
	def withName(countryName: String, ignoreCountriesWithIsoCode: Boolean = false,
	             preferCountriesWithoutIsoCode: Boolean = false)
	            (implicit connection: Connection) =
	{
		val exactNameCondition = model.withName(countryName).toCondition
		val noIsoCodeCondition = model.isoCodeColumn.isNull
		val firstCondition =
		{
			if (ignoreCountriesWithIsoCode || preferCountriesWithoutIsoCode)
				exactNameCondition && noIsoCodeCondition
			else
				exactNameCondition
		}
		// Attempts to find direct name matches first (prefers countries without ISO-code)
		find(firstCondition)
			// If that didn't work, expands the search to countries with an ISO code (if allowed)
			.orElse {
				if (ignoreCountriesWithIsoCode || !preferCountriesWithoutIsoCode)
					None
				else
					factory.getMany(exactNameCondition && model.isoCodeColumn.isNotNull).minByOption { _.name.length }
			}
			// And if that didn't work either, searches with word containment
			.orElse {
				val nameWords = countryName.words
				val wordConditions = nameWords.map { name => model.nameColumn.contains(name) }
				val wordsCondition = wordConditions.head && wordConditions.tail
				val firstWordsCondition =
				{
					if (ignoreCountriesWithIsoCode || preferCountriesWithoutIsoCode)
						wordsCondition && noIsoCodeCondition
					else
						wordsCondition
				}
				// Searches from countries without ISO-code first
				factory.getMany(firstWordsCondition).minByOption { _.name.length }
					// And if that didn't work, includes those with ISO-code (if allowed)
					.orElse {
						if (ignoreCountriesWithIsoCode || !preferCountriesWithoutIsoCode)
							None
						else
							factory.getMany(wordsCondition && model.isoCodeColumn.isNotNull)
								.minByOption { _.name.length }
					}
			}
	}
	
	
	// NESTED   -------------------------------
	
	case class DbCountryById(countryId: Int) extends SingleIdModelAccess[Country](countryId, DbCountry.factory)
	{
		// COMPUTED ---------------------------
		
		/**
		 * @return An access point to individual cities in this country
		 */
		def city = DbCity.inCountryWithId(countryId)
		
		/**
		 * @param connection Implicit DB Connection
		 * @return Dafif code of this country, if available
		 */
		def dafifCode(implicit connection: Connection) = pullAttribute(model.dafifCodeAttName).string
		/**
		 * Updates this country's dafif-code
		 * @param newCode New dafif code
		 * @param connection DB Connection (implicit)
		 * @return Whether a row was updated
		 */
		def dafifCode_=(newCode: String)(implicit connection: Connection) =
			putAttribute(model.dafifCodeAttName, newCode)
		
		/**
		 * @param connection DB Connection (implicit)
		 * @return Id of this country's capital city
		 */
		def capitalId(implicit connection: Connection) = pullAttribute(model.capitalIdAttName).int
		/**
		 * Updates the capital id of this country
		 * @param newCapitalId Id of the new capital of this country
		 * @param connection DB Connection (implicit)
		 * @return Whether a country row was updated
		 */
		def capitalId_=(newCapitalId: Int)(implicit connection: Connection) =
			putAttribute(model.capitalIdAttName, newCapitalId)
		
		/**
		 * @param connection Implicit DB Connection
		 * @return Id of the country which is sovereign over this one
		 */
		def sovereigntyId(implicit connection: Connection) = pullAttribute(model.sovereigntyAttName).int
		/**
		 * Updates the sovereign country link for this country
		 * @param sovereigntyId The id of the country that is over this country
		 * @param connection DB Connection (implicit)
		 * @return Whether a row was updated
		 */
		def sovereigntyId_=(sovereigntyId: Int)(implicit connection: Connection) =
			putAttribute(model.sovereigntyAttName, sovereigntyId)
		
		/**
		 * @param connection Implicit DB connection
		 * @return The capital city of this country, if known
		 */
		def capital(implicit connection: Connection) = connection(
			Select(factory.target join cityTable, cityTable) + Where(condition)).parseSingle(cityFactory)
	}
}
