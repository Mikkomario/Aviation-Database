package vf.aviation.input.controller

import utopia.flow.datastructure.immutable.{Constant, Model, ModelDeclaration}
import utopia.flow.generic.{FromModelFactoryWithSchema, IntType, LocalDateType, StringType}
import utopia.flow.generic.ValueUnwraps._
import utopia.flow.parse.CsvReader
import utopia.flow.time.TimeExtensions._
import utopia.flow.util.CollectionExtensions._
import utopia.flow.util.StringExtensions._
import utopia.flow.util.UncertainBoolean
import utopia.flow.util.UncertainBoolean.{Certain, Undefined}
import utopia.vault.database.Connection
import vf.aviation.core.database.access.single.country.DbCountry
import vf.aviation.core.database.model.country.{CityModel, CountryModel, StateModel, WorldAreaModel, WorldRegionModel}
import vf.aviation.core.model.partial.country.{CityData, CountryData, StateData}
import vf.aviation.core.model.stored.country.{WorldArea, WorldRegion}
import vf.aviation.core.util.StringFormatExtensions._

import java.nio.file.Path
import java.time.LocalDate
import scala.io.Codec

/**
 * Used for importing WAC_COUNTRY_STATE file information as world area codes and country + state.
 * Expects the input as .csv ordered (ascending) by WAC
 * @author Mikko Hilpinen
 * @since 28.3.2021, v0.1
 */
object ImportWacCountryState
{
	// ATTRIBUTES   ---------------------------------
	
	private implicit val codec: Codec = Codec.UTF8
	
	
	// OTHER    -------------------------------------
	
	/**
	 * Imports world area code, country and state information from a .csv file
	 * @param path Path to the file to read
	 * @param separator Separator between the columns (default = ',')
	 * @param connection DB Connection (implicit)
	 * @return Read countries. May contain a failure.
	 */
	def apply(path: Path, separator: String = ",")(implicit connection: Connection) =
	{
		CsvReader.iterateLinesIn(path, separator, ignoreEmptyStringValues = true) { modelsIterator =>
			modelsIterator.mapCatching { WacRow(_) } { _.printStackTrace() }
				// Groups the rows based on the first world area code character
				.groupBy { _.worldRegionCode }
				.flatMap { case (regionCode, rows) =>
					// Inserts a new world region (ignores parenthesis content in region name)
					val region = WorldRegion(regionCode, rows.head.worldRegionName.withoutParenthesisContent)
					WorldRegionModel.insert(region)
					println(s"Handling world region $regionCode ${region.name} (${rows.size} rows)")
					
					// Checks for duplicate world area codes
					/*
					val (duplicateWacRows, uniqueRows) = rows.groupBy { _.worldAreaCode }
						.dividedWith { case (wac, rows) =>
							// Case: Duplicates
							if (rows.size > 1)
								Left(wac -> rows)
							// Case: Unique
							else
								Right(rows.head)
						}*/
					
					// Handles unique rows first
					// Handles one country at a time.
					// Rows without country iso code are each treated as an individual country
					// Collects country + country WAC + sovereignty + state WACs
					rows.groupBy { _.countryIsoCode }.flatMap { case (countryIsoCode, rows) =>
						if (countryIsoCode.isDefined)
						{
							println(s"Handling country $countryIsoCode (${rows.size} rows)")
							Vector(handleCountryRows(regionCode, countryIsoCode, rows))
						}
						else
						{
							println(s"Handling ${rows.size} countries without ISO code")
							rows.map { row => handleCountryRows(regionCode, None, Vector(row)) }
						}
					}.toVector
				}.toVector
		}.map { collectedCountryData =>
			// Records the country -> sovereignty connections
			val countries = collectedCountryData.map { _._1 }
			val sovereigntyNames = collectedCountryData.flatMap { _._2 }.toSet
			val sovereignties = sovereigntyNames.flatMap { countryName =>
				val lowerName = countryName.toLowerCase
				// Finds match by:
				// a) Exact name match (case-insensitive)
				// b) Containment (case-insensitive)
				countries.bestMatch(Vector(
					c => c.name.toLowerCase == lowerName,
					c => c.name.toLowerCase.contains(lowerName))).minByOption { _.name.length }
					.map { countryName -> _ }
			}.toMap
			println(s"Inserted ${countries.size} countries")
			println(s"${sovereigntyNames.size} sovereignties were listed, ${sovereignties.size} found")
			collectedCountryData.foreach { case (country, sovereigntyName) =>
				sovereigntyName.flatMap(sovereignties.get).foreach { sovereignty =>
					DbCountry(country.id).sovereigntyId = sovereignty.id
				}
			}
			countries
		}
	}
	
	private def handleCountryRows(worldRegionId: Int, countryIsoCode: Option[String], rows: Seq[WacRow])
	                             (implicit connection: Connection) =
	{
		// Finds the primary row, which is preferably one without state information and flagged as latest
		val mainRow = rows.bestMatch(Vector(r => r.stateIsoCode.isEmpty, r => r.isLatestVersion)).head
		// Inserts a new country to the database
		val country = CountryModel.insert(CountryData(mainRow.countryName, Some(worldRegionId), countryIsoCode,
			ended = mainRow.ended, comment = if (mainRow.isCountryRow) mainRow.comment else None,
			independent = mainRow.independent))
		println(s"Inserts country: ${country.name} with ${rows.size - 1} other rows")
		
		// Inserts the country world area code & capital (if present and latest)
		if (mainRow.isLatestVersion)
		{
			val countryWorldArea =
			{
				if (mainRow.isCountryRow)
					Some(WorldAreaModel.insert(WorldArea(mainRow.worldAreaCode, mainRow.worldAreaName, country.id,
						mainRow.started.yearMonth, deprecatedAfter = mainRow.ended)))
				else
					None
			}
			// Inserts a new city as the capital, if possible
			mainRow.capitalName.foreach { capitalName =>
				val capital = CityModel.insert(CityData(capitalName.withoutParenthesisContent.untilFirst(","),
					country.id, worldAreaCode = countryWorldArea.map { _.code }))
				// Updates the country to include link to the capital
				DbCountry(country.id).capitalId = capital.id
			}
		}
		
		// Inserts the states and associated world area codes
		val stateData = rows.flatMap { row =>
			row.stateIsoCode.flatMap { stateIsoCode =>
				row.stateName.map { stateName =>
					val stateData = StateData(stateName, country.id, stateIsoCode, row.stateFipsCode, row.comment)
					// World area codes are not inserted if they aren't the latest version of that code
					if (row.isLatestVersion)
					{
						val wacData = WorldArea(row.worldAreaCode, row.worldAreaName, country.id, row.started.yearMonth,
							deprecatedAfter = row.ended)
						stateData -> Some(wacData)
					}
					else
						stateData -> None
				}
			}
		}
		val states = StateModel.insert(stateData.map { _._1 })
		// Inserts the world area codes also
		WorldAreaModel.insert(states.zip(stateData.map { _._2 })
			.flatMap { case (state, area) => area.map { _.copy(stateId = Some(state.id)) } })
		if (states.nonEmpty)
			println(s"Inserted ${states.size} states for ${country.name}")
		
		// Returns generated country and possible sovereignty
		country -> mainRow.sovereignty
	}
	
	
	// NESTED   ------------------------------------
	
	private object WacRow extends FromModelFactoryWithSchema[WacRow]
	{
		// ATTRIBUTES   ----------------------------
		
		override val schema = ModelDeclaration("WAC" -> IntType, "WAC_NAME" -> StringType,
			"WORLD_AREA_NAME" -> StringType, "IS_LATEST" -> IntType, "COUNTRY_SHORT_NAME" -> StringType,
			"START_DATE" -> LocalDateType)
		
		
		// IMPLEMENTED  ----------------------------
		
		override protected def fromValidatedModel(model: Model[Constant]) =
		{
			// independence is parsed from COUNTRY_TYPE (string)
			val countryTypeString = model("COUNTRY_TYPE").getString
			val independence =
			{
				if (countryTypeString.startsWith("I"))
					Certain(true)
				else if (countryTypeString.startsWith("D"))
					Certain(false)
				else
					Undefined
			}
			// IS_LATEST is an integer in the source data, but represents a boolean value (0/1)
			WacRow(model("WAC"), model("WORLD_AREA_NAME"), model("WAC_NAME"), model("COUNTRY_SHORT_NAME"),
				model("START_DATE"), model("COUNTRY_CODE_ISO"), model("STATE_CODE"), model("STATE_FIPS"),
				model("STATE_NAME"), model("CAPITAL"), model("SOVEREIGNTY"), model("THRU_DATE"),
				model("COMMENTS"), independence, model("IS_LATEST").getInt > 0)
		}
	}
	
	private case class WacRow(worldAreaCode: Int, worldRegionName: String, worldAreaName: String, countryName: String,
	                          started: LocalDate, countryIsoCode: Option[String] = None,
	                          stateIsoCode: Option[String] = None, stateFipsCode: Option[Int] = None,
	                          stateName: Option[String] = None, capitalName: Option[String] = None,
	                          sovereignty: Option[String] = None, ended: Option[LocalDate] = None,
	                          comment: Option[String] = None, independent: UncertainBoolean = Undefined,
	                          isLatestVersion: Boolean = false)
	{
		def worldRegionCode = worldAreaCode / 100
		
		def isStateRow = stateIsoCode.isDefined
		def isCountryRow = !isStateRow
	}
}
