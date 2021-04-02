package vf.aviation.input.controller

import utopia.flow.datastructure.immutable.Value
import utopia.flow.generic.ValueConversions._
import utopia.flow.parse.CsvReader
import utopia.flow.time.TimeExtensions._
import utopia.flow.util.CollectionExtensions._
import utopia.flow.util.StringExtensions._
import utopia.genesis.shape.shape1D.Angle
import utopia.genesis.util.Distance
import utopia.vault.database.Connection
import vf.aviation.core.database.access.id.many.DbDaylightSavingZoneCodes
import vf.aviation.core.database.access.many.station.DbStationTypes
import vf.aviation.core.database.access.single.station.DbStation.DbAirport
import vf.aviation.core.database.model.StationModel
import vf.aviation.core.model.cached.Coordinates
import vf.aviation.core.model.enumeration.StandardStationType.Airport

import java.nio.file.Path
import scala.concurrent.duration.Duration
import scala.io.Codec
import scala.util.Failure

/**
 * Imports airports.dat file contents to the database
 * @author Mikko Hilpinen
 * @since 2.4.2021, v0.1
 */
object ImportAirportsDat
{
	// ATTRIBUTES   -----------------------
	
	private implicit val codec: Codec = Codec.UTF8
	
	private val nullString = "\\N"
	
	
	// OTHER    ---------------------------
	
	def apply(path: Path, separator: String = ",")(implicit connection: Connection) =
	{
		// Reads station types from the database
		val stationTypeIdPerCode = DbStationTypes.all.map { t => t.openFlightsCode -> t.id }.toMap
		// Also reads available daylight saving zone codes
		val daylightSavingZoneCodes = DbDaylightSavingZoneCodes.all.toSet
		
		CsvReader.iterateRawRowsIn(path, separator) { rowsIterator =>
			rowsIterator.mapCatching(AirportRow.fromRow) { _.printStackTrace() }
				// Attempts to group rows based on country
				.groupBy { _.countryName }
				.foreach { case (countryName, rows) =>
					val rowsPerType = rows.groupBy { row => row.typeCode.flatMap(stationTypeIdPerCode.get) }
					val (nonAirportRows, airportRows) = rowsPerType.dividedWith { case (typeId, rows) =>
						if (typeId.contains(Airport.id))
							Right(rows)
						else
							Left(typeId -> rows)
					}
					// Checks for existing matches based on the airport IATA code
					val (nonMatchingAirportRows, iataMatchAirports) = airportRows.flatten.dividedWith { row =>
						row.iataCode.flatMap { DbAirport.withIataCode(_) } match
						{
							case Some(existing) => Right(row -> existing)
							case None => Left(row)
						}
					}
					val iataMatchesPerCityId = iataMatchAirports.groupBy { _._2.cityId }
					// TODO: Continue
					// Updates iata match airports and also the associated cities
					iataMatchAirports.foreach { case (row, airport) =>
						// TODO: Update city / make sure one is inserted
						StationModel.withId(airport.id).withOpenFlightsId(row.id).withIcaoCode(row.icaoCode)
							.withAltitude(row.altitude).update()
					}
				}
		}
	}
	
	
	// NESTED   ---------------------------
	
	private object AirportRow
	{
		def fromRow(row: Seq[String]) =
		{
			if (row.size < 13)
				Failure(new IllegalArgumentException(
					s"Airport row must have at least 13 items. Row: ${row.mkString(", ")}"))
			else
			{
				def value(index: Int): Value = row(index).notEmpty.filter { _ != nullString }
				value(4).double.toTry { new IllegalArgumentException(s"Latitude ${row(4)} is invalid") }
					.flatMap { latitude =>
						value(5).double.toTry { new IllegalArgumentException(s"Longitude ${row(5)} is invalid") }
							.map { longitude =>
								AirportRow(value(0).getInt, row(1), row(2), row(3),
									Coordinates(Angle.ofDegrees(latitude), Angle.ofDegrees(longitude)),
									value(6).double.map(Distance.ofFeet), value(7).string, value(8).string,
									value(9).string, value(10).double.map { _.hours }, value(11).string,
									value(12).string)
							}
					}
			}
		}
	}
	
	private case class AirportRow(id: Int, name: String, cityName: String, countryName: String,
	                              coordinates: Coordinates, altitude: Option[Distance] = None,
	                              iataCode: Option[String] = None, icaoCode: Option[String] = None,
	                              timeZoneName: Option[String] = None, timeZoneDifference: Option[Duration] = None,
	                              daylightSavingsZone: Option[String] = None, typeCode: Option[String] = None)
}
