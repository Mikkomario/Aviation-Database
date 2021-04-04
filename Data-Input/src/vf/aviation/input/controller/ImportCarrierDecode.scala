package vf.aviation.input.controller

import utopia.flow.datastructure.immutable.{Constant, Model, ModelDeclaration}
import utopia.flow.generic.{FromModelFactoryWithSchema, IntType, LocalDateType, StringType}
import utopia.flow.generic.ValueUnwraps._
import utopia.flow.parse.CsvReader
import utopia.flow.time.TimeExtensions._
import utopia.flow.time.Today
import utopia.flow.util.CollectionExtensions._
import utopia.vault.database.Connection
import vf.aviation.core.database.access.many.country.DbWorldAreas
import vf.aviation.core.database.model.CarrierModel
import vf.aviation.core.model.enumeration.StandardCarrierSizeCategory.{Large, Major, Medium, Small}
import vf.aviation.core.model.enumeration.StandardCarrierType.{AllCargo, Commuter, SmallCertified}
import vf.aviation.core.model.partial.CarrierData

import java.nio.file.Path
import java.time.LocalDate
import scala.io.Codec

/**
 * Used for reading carrier data from CARRIER DECODE document. Expects the document to be ordered by
 * carrier code (CARRIER column). Expects no carrier data to be imported at this point
 * but expects all country and wolrd area code data to be present.
 * @author Mikko Hilpinen
 * @since 4.4.2021, v0.1
 */
object ImportCarrierDecode
{
	// ATTRIBUTES   -------------------------
	
	private implicit val codec: Codec = Codec.UTF8
	
	private val maxInsertSize = 1500
	
	
	// OTHER    -----------------------------
	
	/**
	 * Reads carrier data from CARRIER DECODE document. Expects the document to be ordered by carrier code.
	 * Also expects country and world area data to be inserted already. No other carrier data should be imported yet.
	 * @param path Path to the file to read
	 * @param separator Separator placed between columns (default = ",")
	 * @param connection DB Connection (implicit)
	 * @return Success or failure
	 */
	def apply(path: Path, separator: String = ",")(implicit connection: Connection) =
	{
		val today = Today.toLocalDate
		CsvReader.iterateLinesIn(path, separator, ignoreEmptyStringValues = true) { linesIterator =>
			// Reads all the world area codes to make sure only those are referred to
			val worldAreas = DbWorldAreas.all.view.map { a => a.code -> a }.toMap
			var missingWorldAreas = Set[Int]()
			
			linesIterator.mapCatching(CarrierRow.apply) { _.printStackTrace() }
				// Handles one IATA / ICAO code at a time
				.groupBy { _.code }
				.map { case (code, rows) =>
					// Finds the rows that haven't been ended or that represent the last version
					// There may be many rows, but they should contain approximately the same content
					val row = rows.bestMatch(Vector(r => r.endDate.isEmpty)).maxBy { _.endDate.getOrElse(today) }
					
					// Finds matching world area, if present
					val worldArea = worldAreas.get(row.worldAreaCode)
					// Records possibly missing world area codes to be logged later
					if (worldArea.isEmpty && !missingWorldAreas.contains(row.worldAreaCode))
						missingWorldAreas += row.worldAreaCode
					
					// Interprets some enumeration values
					val sizeCategory = row.groupId match
					{
						case 4 => Some(Small)
						case 1 => Some(Medium)
						case 2 => Some(Large)
						case 3 => Some(Major)
						case _ => None
					}
					val typeCategory = row.groupId match
					{
						case 6 | 9 => Some(Commuter)
						case 7 => Some(AllCargo)
						case 5 => Some(SmallCertified)
						case _ => None
					}
					
					// Prepares data for insert
					CarrierData(row.name, dotId = Some(row.id), iataCode = Some(code).filter { _.length == 2 },
						icaoCode = Some(code).filter { _.length == 3 }, countryId = worldArea.map { _.countryId },
						worldAreaCode = worldArea.map { _.code }, sizeCategoryId = sizeCategory.map { _.id },
						typeCategoryId = typeCategory.map { _.id }, started = Some(row.startDate.yearMonth),
						ended = row.endDate, isClosed = row.endDate.isDefined)
				}
				// Inserts the new carriers in bulks
				.foreachGroup(maxInsertSize) { carrierData =>
					println(s"Inserting ${carrierData.size} new carriers to the DB")
					CarrierModel.insert(carrierData)
				}
			
			// Logs a warning for missing world area codes
			if (missingWorldAreas.nonEmpty)
				println(s"Following world area codes couldn't be found from the DB: [${
					missingWorldAreas.toVector.sorted.mkString(", ")}]")
		}
	}
	
	
	// NESTED   -----------------------------
	
	private object CarrierRow extends FromModelFactoryWithSchema[CarrierRow]
	{
		override val schema = ModelDeclaration("AIRLINE_ID" -> IntType, "CARRIER" -> StringType,
			"CARRIER_NAME" -> StringType, "WAC" -> IntType, "CARRIER_GROUP_NEW" -> IntType,
			"START_DATE_SOURCE" -> LocalDateType)
		
		override protected def fromValidatedModel(model: Model[Constant]) =
			CarrierRow(model("AIRLINE_ID"), model("CARRIER"), model("CARRIER_NAME"), model("WAC"),
				model("CARRIER_GROUP_NEW"), model("START_DATE_SOURCE"), model("THRU_DATE_SOURCE"))
	}
	
	private case class CarrierRow(id: Int, code: String, name: String, worldAreaCode: Int, groupId: Int,
	                              startDate: LocalDate, endDate: Option[LocalDate] = None)
}
