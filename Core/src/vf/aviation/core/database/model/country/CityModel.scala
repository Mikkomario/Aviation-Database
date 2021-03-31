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
	/**
	 * @param data City data
	 * @return A model matching that data
	 */
	def apply(data: CityData): CityModel = apply(None, Some(data.name), Some(data.countryId), data.marketId,
		data.stateId, data.worldAreaCode, data.timeZone)
	
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
                     timeZone: Option[Duration] = None)
	extends StorableWithFactory[City]
{
	override def factory = CityFactory
	
	override def valueProperties = Vector("id" -> id, "name" -> name, "countryId" -> countryId,
		"marketId" -> marketId, "stateId" -> stateId, "worldAreaCode" -> worldAreaCode,
		"timeZone" -> timeZone.map { _.toPreciseHours })
}