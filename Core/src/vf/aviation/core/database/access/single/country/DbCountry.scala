package vf.aviation.core.database.access.single.country

import utopia.flow.generic.ValueConversions._
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
	
	
	// NESTED   -------------------------------
	
	case class DbCountryById(countryId: Int) extends SingleIdModelAccess[Country](countryId, DbCountry.factory)
	{
		// COMPUTED ---------------------------
		
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
