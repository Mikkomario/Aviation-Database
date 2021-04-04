package vf.aviation.input.controller

import utopia.flow.parse.CsvReader

import scala.util.Try
import utopia.flow.util.CollectionExtensions._
import utopia.flow.util.StringExtensions._
import utopia.vault.database.Connection
import vf.aviation.core.database.access.single.DbCarrier
import vf.aviation.core.database.access.single.country.DbCountry
import vf.aviation.core.database.model.CarrierModel
import vf.aviation.core.model.partial.CarrierData

import java.nio.file.Path

/**
 * Used for importing Open Flights airlines.dat document contents as carriers.
 * Expects data to be ordered by country name.
 * @author Mikko Hilpinen
 * @since 4.4.2021, v0.1
 */
object ImportAirlinesDat
{
	// ATTRIBUTES   --------------------------
	
	private val nullString = "\\N"
	private val maxInsertSize = 1500
	
	
	// OTHER    ------------------------------
	
	/**
	 * Processes airlines.dat file from the specified path. The process is better and faster if the
	 * document is ordered by country name first.
	 * @param path Path to the file to read
	 * @param separator Separator between columns (default = ",")
	 * @param connection DB Connection (implicit)
	 * @return Success or failure
	 */
	def apply(path: Path, separator: String = ",")(implicit connection: Connection) =
	{
		CsvReader.iterateRawRowsIn(path, separator) { rowIterator =>
			rowIterator.mapCatching(AirlineRow.fromRow) { _.printStackTrace() }
				// Processes rows in country-specific groups
				.groupBy { _.countryName }
				.flatMap { case (countryName, rows) =>
					// Finds the carriers that already exist in the database
					val (newRows, matchingRows) = rows.dividedWith { row =>
						row.icaoCode.flatMap { DbCarrier.forIcaoCode(_) }
							.orElse { row.iataCode.flatMap { DbCarrier.forIataCode(_) } } match
						{
							case Some(existing) => Right(existing -> row)
							case None => Left(row)
						}
					}
					// Finds id for the target country, if possible. Prefers existing country links.
					val countryId = matchingRows.findMap { _._1.countryId }.orElse {
						// Uses name-based search if necessary
						countryName.flatMap { DbCountry.withName(_).map { _.id } }
					}
					// Updates the existing row matches
					matchingRows.foreach { case (existing, row) =>
						CarrierModel.withId(existing.id).copy(alias = row.alias, iataCode = row.iataCode,
							icaoCode = row.icaoCode, callSign = row.validCallSign,
							countryId = if (existing.countryId.isDefined) None else countryId,
							openFlightsId = Some(row.id), isClosed = Some(row.closed).filter { !_ }).update()
					}
					// Prepares new rows for insert
					newRows.map { row => CarrierData(row.name, row.alias, row.validCallSign, None, Some(row.id),
						row.iataCode, row.icaoCode, countryId, isClosed = row.closed) }
				}
				.foreachGroup(maxInsertSize) { carrierData =>
					println(s"Inserting ${carrierData.size} new carriers to DB")
					CarrierModel.insert(carrierData)
				}
		}
	}
	
	
	// NESTED   ------------------------------
	
	private object AirlineRow
	{
		def fromRow(row: Seq[String]) =
		{
			def opt(index: Int) = row(index).notEmpty.filter { _ != nullString }
			Try { row.head.toInt }.map { id =>
				apply(id, row(1), opt(2), opt(3).filter { _.length == 2 },
					opt(4).filter { _.length == 3 }, opt(5), opt(6), row(7) != "N")
			}
		}
	}
	
	private case class AirlineRow(id: Int, name: String, alias: Option[String] = None,
	                              iataCode: Option[String] = None, icaoCode: Option[String] = None,
	                              callSign: Option[String] = None, countryName: Option[String] = None,
	                              active: Boolean = true)
	{
		def closed = !active
		
		def validCallSign = callSign.filterNot { callSing =>
			val s = callSing.toLowerCase
			s == "inc." || s.startsWith("s.a.") || s.startsWith("s.l.") || s.startsWith("ltd.") || s.length > 48
		}
	}
}
