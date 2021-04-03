package vf.aviation.core.database.model

import utopia.flow.generic.ValueConversions._
import utopia.genesis.shape.shape1D.Angle
import utopia.genesis.util.Distance
import utopia.vault.database.Connection
import utopia.vault.model.immutable.StorableWithFactory
import utopia.vault.sql.Insert
import vf.aviation.core.database.factory.station.StationFactory
import vf.aviation.core.model.enumeration.StandardStationType
import vf.aviation.core.model.enumeration.StandardStationType.Airport
import vf.aviation.core.model.partial.station.StationData
import vf.aviation.core.model.stored.station
import vf.aviation.core.model.stored.station.Station

import java.time.LocalDate

object StationModel
{
	// ATTRIBUTES   ---------------------
	
	/**
	 * Name of the attribute that contains station name
	 */
	val nameAttName = "name"
	/**
	 * Name of the attribute that contains station type id
	 */
	val typeIdAttName = "typeId"
	/**
	 * Name of the attribute that contains station iata code
	 */
	val iataCodeAttName = "iataCode"
	
	
	// COMPUTED -------------------------
	
	/**
	 * @return Factory used by this model
	 */
	def factory = StationFactory
	/**
	 * @return Table used by this model
	 */
	def table = factory.table
	
	/**
	 * @return Column that contains airport name
	 */
	def nameColumn = table(nameAttName)
	/**
	 * @return Column that contains station type id
	 */
	def typeIdColumn = table(typeIdAttName)
	/**
	 * @return Column that contains station iata code
	 */
	def iataCodeColumn = table(iataCodeAttName)
	
	/**
	 * @return A model that has been marked as an airport
	 */
	def airport = withType(Airport)
	
	
	// OTHER    ------------------------
	
	/**
	 * @param stationId A station id
	 * @return A model with that id
	 */
	def withId(stationId: Int) = apply(Some(stationId))
	
	/**
	 * @param stationType A station type
	 * @return A model with that station type
	 */
	def withType(stationType: StandardStationType) = withTypeId(stationType.id)
	/**
	 * @param stationTypeId Station type id
	 * @return A model with type id set
	 */
	def withTypeId(stationTypeId: Int) = apply(typeId = Some(stationTypeId))
	
	/**
	 * @param iataCode Airport / station iata code
	 * @return A model with that iata code
	 */
	def withIataCode(iataCode: String) = apply(iataCode = Some(iataCode))
	
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
	def insert(data: StationData)(implicit connection: Connection) = station.Station(apply(data).insert().getInt, data)
	/**
	 * Inserts multiple new stations to the DB
	 * @param data Data to insert
	 * @param connection DB Connection (implicit)
	 * @return Newly inserted stations
	 */
	def insert(data: Seq[StationData])(implicit connection: Connection) =
	{
		val ids = Insert(table, data.map { apply(_).toModel }).generatedIntKeys
		ids.zip(data).map { case (id, data) => station.Station(id, data) }
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
	import StationModel._
	
	// IMPLEMENTED  ---------------------------
	
	override def factory = StationModel.factory
	
	override def valueProperties = Vector("id" -> id, nameAttName -> name,
		"latitudeNorth" -> latitudeNorth.map { _.degrees }, "longitudeEast" -> longitudeEast.map { _.degrees },
		"altitudeFeet" -> altitude.map { _.toFeet }, typeIdAttName -> typeId, "dotId" -> dotId,
		"openFlightsId" -> openFlightsId, "iataCode" -> iataCode, "icaoCode" -> icaoCode, "cityId" -> cityId,
		"started" -> started, "closed" -> closed, "isClosed" -> isClosed)
	
	
	// OTHER    -------------------------------
	
	/**
	 * @param id Id in the open flights system
	 * @return A copy of this model with that id
	 */
	def withOpenFlightsId(id: Int) = copy(openFlightsId = Some(id))
	
	/**
	 * @param icaoCode Icao standard code
	 * @return A copy of this model with that icao code
	 */
	def withIcaoCode(icaoCode: Option[String]) = copy(icaoCode = icaoCode)
	
	/**
	 * @param altitude Station altitude / elevation
	 * @return A copy of this model with that altitude
	 */
	def withAltitude(altitude: Option[Distance]) = copy(altitude = altitude)
}