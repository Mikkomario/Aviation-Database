package vf.aviation.core.database.model

import utopia.flow.generic.ValueConversions._
import utopia.flow.time.TimeExtensions._
import utopia.vault.database.Connection
import utopia.vault.model.immutable.StorableWithFactory
import utopia.vault.sql.Insert
import vf.aviation.core.database.factory.CarrierFactory
import vf.aviation.core.model.partial.CarrierData
import vf.aviation.core.model.stored.Carrier

import java.time.LocalDate

object CarrierModel
{
	// COMPUTED --------------------------
	
	/**
	 * @return Factory used by this model
	 */
	def factory = CarrierFactory
	/**
	 * @return Table used by this model
	 */
	def table = factory.table
	
	
	// OTHER    --------------------------
	
	/**
	 * @param data Carrier data
	 * @return A model based on that data
	 */
	def apply(data: CarrierData): CarrierModel = apply(None, Some(data.name), data.alias, data.callSign, data.dotId,
		data.openFlightsId, data.iataCode, data.icaoCode, data.countryId, data.worldAreaCode, data.sizeCategoryId,
		data.typeCategoryId, data.started.map { _.firstDay }, data.ended, Some(data.isClosed))
	
	/**
	 * Inserts a new carrier to the db
	 * @param data Data to insert
	 * @param connection DB Connection (implicit)
	 * @return Newly inserted carrier
	 */
	def insert(data: CarrierData)(implicit connection: Connection) = Carrier(apply(data).insert().getInt, data)
	/**
	 * Inserts multiple new carriers to the db
	 * @param data Data to insert
	 * @param connection DB Connection (implicit)
	 * @return Newly inserted carriers
	 */
	def insert(data: Seq[CarrierData])(implicit connection: Connection) =
	{
		val ids = Insert(table, data.map { apply(_).toModel }).generatedIntKeys
		ids.zip(data).map { case (id, data) => Carrier(id, data) }
	}
}

/**
 * Used for interacting with carrier data in the DB
 * @author Mikko Hilpinen
 * @since 4.4.2021, v0.1
 */
case class CarrierModel(id: Option[Int] = None, name: Option[String] = None, alias: Option[String] = None,
                        callSign: Option[String] = None, dotId: Option[Int] = None, openFlightsId: Option[Int] = None,
                        iataCode: Option[String] = None, icaoCode: Option[String] = None, countryId: Option[Int] = None,
                        worldAreaCode: Option[Int] = None, sizeCategoryId: Option[Int] = None,
                        typeCategoryId: Option[Int] = None, started: Option[LocalDate] = None,
                        ended: Option[LocalDate] = None, isClosed: Option[Boolean] = None)
	extends StorableWithFactory[Carrier]
{
	override def factory = CarrierModel.factory
	
	override def valueProperties = Vector("id" -> id, "name" -> name, "alias" -> alias, "callSign" -> callSign,
		"dotId" -> dotId, "openFlightsId" -> openFlightsId, "iataCode" -> iataCode, "icaoCode" -> icaoCode,
		"countryId" -> countryId, "worldAreaCode" -> worldAreaCode, "sizeCategoryId" -> sizeCategoryId,
		"typeCategoryId" -> typeCategoryId, "started" -> started, "ended" -> ended, "isClosed" -> isClosed)
}
