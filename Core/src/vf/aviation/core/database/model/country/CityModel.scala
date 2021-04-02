package vf.aviation.core.database.model.country

import utopia.flow.generic.ValueConversions._
import utopia.flow.time.TimeExtensions._
import utopia.vault.database.Connection
import utopia.vault.model.immutable.StorableWithFactory
import vf.aviation.core.database.factory.country.CityFactory
import vf.aviation.core.model.partial.country.CityData
import vf.aviation.core.model.stored.country.City

import scala.concurrent.duration.Duration

object CityModel
{
	// ATTRIBUTES   --------------------
	
	/**
	 * Name of the attribute that contains the linked country's id
	 */
	val countryIdAttName = "countryId"
	/**
	 * Name of the attribute that contains city's name
	 */
	val nameAttName = "name"
	
	
	// COMPUTED ------------------------
	
	/**
	 * @return Factory used by this model
	 */
	def factory = CityFactory
	/**
	 * @return Table used by this model
	 */
	def table = factory.table
	
	/**
	 * @return Column that contains the city name
	 */
	def nameColumn = table(nameAttName)
	
	
	// OTHER    ------------------------
	
	/**
	 * @param cityId City id
	 * @return A model with only id set
	 */
	def withId(cityId: Int) = apply(Some(cityId))
	
	/**
	 * @param countryId Country id
	 * @return A model containing that country id
	 */
	def withCountryId(countryId: Int) = apply(countryId = Some(countryId))
	
	/**
	 * @param cityName Name of a city
	 * @return A model with that name
	 */
	def withName(cityName: String) = apply(name = Some(cityName))
	
	/**
	 * @param data City data
	 * @return A model matching that data
	 */
	def apply(data: CityData): CityModel = apply(None, Some(data.name), Some(data.countryId), data.marketId,
		data.stateId, data.worldAreaCode, data.timeZone, data.timeZoneName, data.daylightSavingZoneCode)
	
	/**
	 * Inserts a new city to the database
	 * @param data Data to insert
	 * @param connection DB Connection (implicit)
	 * @return Newly inserted city
	 */
	def insert(data: CityData)(implicit connection: Connection) = City(apply(data).insert().getInt, data)
}

/**
 * Used for interacting with city data in DB
 * @author Mikko Hilpinen
 * @since 31.3.2021, v0.1
 */
case class CityModel(id: Option[Int] = None, name: Option[String] = None, countryId: Option[Int] = None,
                     marketId: Option[Int] = None, stateId: Option[Int] = None, worldAreaCode: Option[Int] = None,
                     timeZone: Option[Duration] = None, timeZoneName: Option[String] = None,
                     daylightSavingZoneCode: Option[Char] = None)
	extends StorableWithFactory[City]
{
	import CityModel._
	
	// IMPLEMENTED  ------------------------
	
	override def factory = CityModel.factory
	
	override def valueProperties = Vector("id" -> id, nameAttName -> name, countryIdAttName -> countryId,
		"marketId" -> marketId, "stateId" -> stateId, "worldAreaCode" -> worldAreaCode,
		"timeZone" -> timeZone.map { _.toPreciseHours }, "timeZoneName" -> timeZoneName,
		"daylightSavingZoneCode" -> daylightSavingZoneCode.map { _.toString })
	
	
	// OTHER    ----------------------------
	
	/**
	 * @param cityMarketId City market id
	 * @return A copy of this model with that market id
	 */
	def withMarketId(cityMarketId: Int) = copy(marketId = Some(cityMarketId))
	
	/**
	 * @param code World area code
	 * @return A copy of this model with that world area code
	 */
	def withWorldAreaCode(code: Int) = copy(worldAreaCode = Some(code))
	/**
	 * @param stateId State id
	 * @return A copy of this model with that state id
	 */
	def withStateId(stateId: Option[Int]) = copy(stateId = stateId)
	
	/**
	 * @param timeZone Time zone difference from UTC time
	 * @return A copy of this model with that time zone information
	 */
	def withTimeZone(timeZone: Option[Duration]) = copy(timeZone = timeZone)
	/**
	 * @param timeZoneName Time zone name
	 * @return A copy of this model with that time zone name
	 */
	def withTimeZoneName(timeZoneName: Option[String]) = copy(timeZoneName = timeZoneName)
	/**
	 * @param code A daylight saving zone code
	 * @return Copy of this model with that code
	 */
	def withDaylightSavingZoneCode(code: Option[Char]) = copy(daylightSavingZoneCode = code)
}