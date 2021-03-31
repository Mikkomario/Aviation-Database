package vf.aviation.core.database.model.country

import utopia.flow.generic.ValueConversions._
import utopia.vault.database.Connection
import utopia.vault.model.immutable.StorableWithFactory
import vf.aviation.core.database.factory.country.CountryFactory
import vf.aviation.core.model.partial.country.CountryData
import vf.aviation.core.model.stored.country.Country

import java.time.LocalDate

object CountryModel
{
	// ATTRIBUTES   --------------------
	
	/**
	 * Name of the attribute which contains the id of the capital city of a country
	 */
	val capitalIdAttName = "capitalId"
	/**
	 * Name of the attribute that referst to the sovereign country over a country
	 */
	val sovereigntyAttName = "sovereigntyCountryId"
	
	
	// COMPUTED ------------------------
	
	/**
	 * @return Factory used by this model
	 */
	def factory = CountryFactory
	
	
	// OTHER    ------------------------
	
	/**
	 * @param countryId Country id
	 * @return A model with that id
	 */
	def withId(countryId: Int) = apply(Some(countryId))
	
	/**
	 * @param data A country data model
	 * @return A model matching that data
	 */
	def apply(data: CountryData): CountryModel = apply(None, Some(data.name), data.worldRegionId, data.isoCode,
		data.dafifCode, data.capitalId, data.sovereigntyCountryId, data.ended, data.comment, data.independent)
	
	/**
	 * Inserts a new country to the database
	 * @param data Data to insert
	 * @param connection DB Connection (implicit)
	 * @return Inserted country
	 */
	def insert(data: CountryData)(implicit connection: Connection) = Country(apply(data).insert().getInt, data)
}

/**
 * Used for interacting with country data in DB
 * @author Mikko Hilpinen
 * @since 31.3.2021, v0.1
 */
case class CountryModel(id: Option[Int] = None, name: Option[String] = None, worldRegionId: Option[Int] = None,
                        isoCode: Option[String] = None, dafifCode: Option[String] = None, capitalId: Option[Int] = None,
                        sovereigntyCountryId: Option[Int] = None, ended: Option[LocalDate] = None,
                        comment: Option[String] = None, independent: Option[Boolean] = None)
	extends StorableWithFactory[Country]
{
	import CountryModel._
	
	// IMPLEMENTED  ---------------------------
	
	override def factory = CountryModel.factory
	
	override def valueProperties = Vector("id" -> id, "name" -> name, "worldRegionId" -> worldRegionId,
		"isoCode" -> isoCode, "dafifCode" -> dafifCode, capitalIdAttName -> capitalId,
		"sovereigntyCountryId" -> sovereigntyCountryId, "ended" -> ended, "comment" -> comment,
		"independent" -> independent)
	
	
	// OTHER    ------------------------------
	
	/**
	 * @param capitalId Id of the capital city of this country
	 * @return A model with capital city id set
	 */
	def withCapitalId(capitalId: Int) = copy(capitalId = Some(capitalId))
}
