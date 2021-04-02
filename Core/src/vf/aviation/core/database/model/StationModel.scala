package vf.aviation.core.database.model

import utopia.flow.generic.ValueConversions._
import utopia.genesis.shape.shape1D.Angle
import utopia.genesis.util.Distance
import utopia.vault.database.Connection
import utopia.vault.model.immutable.StorableWithFactory
import utopia.vault.sql.Insert
import vf.aviation.core.database.factory.StationFactory
import vf.aviation.core.model.partial.StationData
import vf.aviation.core.model.stored.Station

import java.time.LocalDate

object StationModel
{
	// COMPUTED -------------------------
	
	/**
	 * @return Factory used by this model
	 */
	def factory = StationFactory
	/**
	 * @return Table used by this model
	 */
	def table = factory.table
	
	// OTHER    ------------------------
	
	/**
	 * @param data Station data
	 * @return A model matching that data
	 */
	def apply(data: StationData): StationModel = apply(None, Some(data.name), Some(data.coordinates.latitudeNorth),
		Some(data.coordinates.longitudeEast), data.altitude, data.stationTypeId, data.dotId, data.openFlightsId,
		data.iataCode, data.icaoCode, data.cityId, data.started, data.closed, Some(data.isClosed))
	
	/**
	 * Inserts a new station to the DB
	 * @param data Data to insert
	 * @param connection DB Connection (implicit)
	 * @return Newly inserted station
	 */
	def insert(data: StationData)(implicit connection: Connection) = Station(apply(data).insert().getInt, data)
	/**
	 * Inserts multiple new stations to the DB
	 * @param data Data to insert
	 * @param connection DB Connection (implicit)
	 * @return Newly inserted stations
	 */
	def insert(data: Seq[StationData])(implicit connection: Connection) =
	{
		val ids = Insert(table, data.map { apply(_).toModel }).generatedIntKeys
		ids.zip(data).map { case (id, data) => Station(id, data) }
	}
}

/**
 * Used for interacting with station data in the DB
 * @author Mikko Hilpinen
 * @since 2.4.2021, v0.1
 */
case class StationModel(id: Option[Int] = None, name: Option[String] = None, latitudeNorth: Option[Angle] = None,
                        longitudeEast: Option[Angle] = None, altitude: Option[Distance] = None,
                        typeId: Option[Int] = None, dotId: Option[Int] = None, openFlightsId: Option[Int] = None,
                        iataCode: Option[String] = None, icaoCode: Option[String] = None, cityId: Option[Int] = None,
                        started: Option[LocalDate] = None, closed: Option[LocalDate] = None,
                        isClosed: Option[Boolean] = None)
	extends StorableWithFactory[Station]
{
	override def factory = StationModel.factory
	
	override def valueProperties = Vector("id" -> id, "name" -> name, "latitudeNorth" -> latitudeNorth.map { _.degrees },
		"longitudeEast" -> longitudeEast.map { _.degrees }, "altitudeFeet" -> altitude.map { _.toFeet },
		"typeId" -> typeId, "dotId" -> dotId, "openFlightsId" -> openFlightsId, "iataCode" -> iataCode,
		"icaoCode" -> icaoCode, "cityId" -> cityId, "started" -> started, "closed" -> closed, "isClosed" -> isClosed)
}