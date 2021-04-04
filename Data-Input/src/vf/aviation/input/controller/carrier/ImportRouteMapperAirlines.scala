package vf.aviation.input.controller.carrier

import utopia.flow.datastructure.immutable.{Constant, Model, ModelDeclaration}
import utopia.flow.generic.{FromModelFactoryWithSchema, StringType}
import utopia.flow.generic.ValueUnwraps._
import utopia.flow.parse.CsvReader
import utopia.flow.util.CollectionExtensions._
import utopia.vault.database.Connection
import vf.aviation.core.database.access.id.single.{DbCarrierIataCode, DbCarrierIcaoCode}
import vf.aviation.core.database.model.CarrierModel
import vf.aviation.core.model.partial.CarrierData

import java.nio.file.Path
import scala.io.Codec

/**
 * Imports airline data from route mapper airlines.dat document
 * @author Mikko Hilpinen
 * @since 4.4.2021, v0.1
 */
object ImportRouteMapperAirlines
{
	// ATTRIBUTES   ---------------------
	
	private implicit val codec: Codec = Codec.UTF8
	
	
	// OTHER    -------------------------
	
	/**
	 * Reads airline information from the Route Mapper airlines.dat file. Expects there to exist some airline
	 * information in the DB already
	 * @param path Path to read
	 * @param separator Separator between columns (default = ";")
	 * @param connection DB Connection (implicit)
	 * @return Read carrier data. May contain a failure.
	 */
	def apply(path: Path, separator: String = ";")(implicit connection: Connection) =
	{
		CsvReader.iterateLinesIn(path, separator) { linesIterator =>
			val carrierData = linesIterator.mapCatching(AirlineRow.apply) { _.printStackTrace() }
				.flatMap { row =>
					// Ignores rows that already exist in the database. Inserts the rest.
					row.code.length match
					{
						// Case: Iata code
						case 2 =>
							if (DbCarrierIataCode(row.code).isEmpty)
								Some(CarrierData(row.name, iataCode = Some(row.code)))
							else
								None
						// Case: Icao code
						case 3 =>
							if (DbCarrierIcaoCode(row.code).isEmpty)
								Some(CarrierData(row.name, icaoCode = Some(row.code)))
							else
								None
						// Case: Invalid code
						case _ =>
							println(s"Invalid airline row: ${row.code} ${row.name}")
							None
					}
				}.toVector
			
			// Inserts all data at once since there is a maximum of 700 rows in the document anyway
			CarrierModel.insert(carrierData)
		}
	}
	
	
	// NESTED   -------------------------
	
	private object AirlineRow extends FromModelFactoryWithSchema[AirlineRow]
	{
		override val schema = ModelDeclaration("airline-id" -> StringType, "airline-name" -> StringType)
		
		override protected def fromValidatedModel(model: Model[Constant]) =
			AirlineRow(model("airline-id"), model("airline-name"))
	}
	
	private case class AirlineRow(code: String, name: String)
}
