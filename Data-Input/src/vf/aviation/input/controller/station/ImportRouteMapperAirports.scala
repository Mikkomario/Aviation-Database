package vf.aviation.input.controller.station

import utopia.flow.datastructure.immutable.{Constant, Model, ModelDeclaration}
import utopia.flow.generic.{DoubleType, FromModelFactoryWithSchema, StringType}
import utopia.flow.generic.ValueUnwraps._
import utopia.flow.parse.CsvReader
import utopia.flow.util.CollectionExtensions._
import utopia.flow.util.StringExtensions._
import utopia.genesis.shape.shape1D.Angle
import utopia.vault.database.Connection
import vf.aviation.core.database.access.id.many.AirportIataCodes
import vf.aviation.core.database.model.StationModel
import vf.aviation.core.model.cached.Coordinates
import vf.aviation.core.model.enumeration.StandardStationType.Airport
import vf.aviation.core.model.partial.station.StationData

import java.nio.file.Path
import scala.io.Codec

/**
 * Imports airport data from the Route Mapper airports.dat file. Expects airport, country and city data to be
 * mostly imported at this point. Expects rows to be ordered by IATA code.
 * @author Mikko Hilpinen
 * @since 3.4.2021, v0.1
 */
object ImportRouteMapperAirports
{
	// ATTRIBUTES   -------------------------
	
	private implicit val codec: Codec = Codec.UTF8
	// Number of iata code characters to form a group
	private val groupLength = 1
	private val maxInsertSize = 1500
	
	
	// OTHER    -----------------------------
	
	/**
	 * Reads and processes route mapper airport data by inserting new stations to DB
	 * @param path       Path to the file to read
	 * @param separator  Separator between columns (default = ";")
	 * @param connection DB Connection (implicit)
	 * @return Success or failure
	 */
	def apply(path: Path, separator: String = ";")(implicit connection: Connection) =
	{
		CsvReader.iterateLinesIn(path, separator, ignoreEmptyStringValues = true) { linesIterator =>
			linesIterator.mapCatching(AirportRow.apply) { _.printStackTrace() }
				// Apparently some iata codes are too long, in which case ignores them
				.filter { _.iataCode.length == 3 }
				// Processes rows in groups based on the two first characters of their iata-code
				.groupBy { _.iataCode.take(groupLength) }
				.flatMap { case (codeBeginning, rows) =>
					// Finds existing similar iata codes from the DB
					val existingIataCodeEnds = AirportIataCodes.startingWith(codeBeginning)
						.map { _.drop(groupLength) }.toSet
					// Only processes rows which are not yet recorded in the database
					rows.filterNot { row => existingIataCodeEnds.contains(row.iataCode.drop(groupLength)) }
						.flatMap { row =>
							// Forms a new airport from each row that has a name
							row.name.map { airportName =>
								StationData(airportName, row.coordinates, iataCode = Some(row.iataCode),
									stationTypeId = Some(Airport.id))
							}
						}
				}
				// Inserts the rows to DB in bulks
				.foreachGroup(maxInsertSize) { stationData =>
					println(s"Inserting ${stationData.size} new airports to DB")
					StationModel.insert(stationData)
				}
		}
	}
	
	
	// NESTED   -----------------------------
	
	private object AirportRow extends FromModelFactoryWithSchema[AirportRow]
	{
		override val schema = ModelDeclaration("airport-id" -> StringType, "latitude" -> DoubleType,
			"longitude" -> DoubleType, "airport-name" -> StringType)
		
		override protected def fromValidatedModel(model: Model[Constant]) =
			AirportRow(model("airport-id"),
				Coordinates(Angle.ofDegrees(model("latitude")), Angle.ofDegrees(model("longitude"))),
				model("airport-name"))
	}
	
	private case class AirportRow(iataCode: String, coordinates: Coordinates, rawName: String)
	{
		// Ignores empty & XXX names
		def name = rawName.trim.notEmpty.filterNot { _.forall { _ == 'X' } }
			// Also removes , from the beginning of some names
			.map { s => if (s.startsWith(",")) s.drop(1).trim else s }
			// Capitalizes the first character of each word. Leaves the rest in lower case.
			.map { _.split(" ").map { _.toLowerCase.capitalize }.mkString(" ") }
	}
	
}
