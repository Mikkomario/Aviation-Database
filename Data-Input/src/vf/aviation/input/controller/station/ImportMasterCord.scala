package vf.aviation.input.controller.station

import utopia.flow.datastructure.immutable.{Constant, Model, ModelDeclaration}
import utopia.flow.generic.{DoubleType, FromModelFactoryWithSchema, IntType, LocalDateType, StringType}
import utopia.flow.generic.ValueUnwraps._
import utopia.flow.parse.CsvReader
import utopia.flow.time.TimeExtensions._
import utopia.flow.util.CollectionExtensions._
import utopia.flow.util.StringExtensions._
import utopia.genesis.shape.shape1D.Angle
import utopia.vault.database.Connection
import vf.aviation.core.database.access.many.country.DbCities
import vf.aviation.core.database.access.single.country.{DbCountry, DbState, DbWorldArea}
import vf.aviation.core.database.model.StationModel
import vf.aviation.core.database.model.country.{CityModel, CountryModel, StateModel, WorldAreaModel}
import vf.aviation.core.model.cached.Coordinates
import vf.aviation.core.model.enumeration.StandardStationType.Airport
import vf.aviation.core.model.partial.country.{CityData, CountryData, StateData}
import vf.aviation.core.model.partial.station
import vf.aviation.core.model.stored.country.WorldArea

import java.nio.file.Path
import java.time.LocalDate
import scala.collection.mutable
import scala.io.Codec

/**
 * Imports airport and city information from the MASTER_CORD file. Expects WAC_COUNTRY_STATE file to be
 * imported already. Please also remember to remove the "Unknown point in Alaska" row at the end of the document.
 * @author Mikko Hilpinen
 * @since 2.4.2021, v0.1
 */
object ImportMasterCord
{
	// ATTRIBUTES   -----------------------
	
	private implicit val codec: Codec = Codec.UTF8
	
	private val maxInsertSize = 1500
	
	
	// OTHER    ---------------------------
	
	/**
	 * Imports city and airport data from the MASTER_CORD file
	 * @param path       Path to the MASTER_CORD file
	 * @param separator  Separator between columns (default = ",")
	 * @param connection DB Connection (implicit)
	 * @return Success or failure
	 */
	def apply(path: Path, separator: String = ",")(implicit connection: Connection) =
	{
		// Collects pre-existing city information for name => id matching
		val existingCityIdsByName = DbCities.all.map { city => city.name.toLowerCase -> city.id }.toMap
		
		// Imports data from the file
		CsvReader.iterateLinesIn(path, separator, ignoreEmptyStringValues = true) { modelsIterator =>
			// City market id -> city id
			val capturedMarketAndCityIds = mutable.Map[Int, Int]()
			
			modelsIterator.mapCatching(AirportRow.apply) { _.printStackTrace() }
				// Only targets the latest airport versions
				.filter { _.isLatestVersion }
				.map { row =>
					// Checks whether that city market was registered already. Uses cached city id if possible.
					val cityId = capturedMarketAndCityIds.getOrElse(row.cityMarketId, {
						// Finds target country + state
						val (countryId, stateId) = row.stateIsoCode match {
							// Case: State ISO-code provided => connects to the matching state
							case Some(stateIsoCode) =>
								DbState.withIsoCode(stateIsoCode) match {
									// Case: State found => connects to that state
									case Some(state) => state.countryId -> Some(state.id)
									// Case: State not found => inserts a new state if possible and connects to that
									case None =>
										val (countryId, stateId) = findCountryAndStateWithoutStateIsoCode(row)
										// Case: State was found in the end (bug / data problem)
										if (stateId.isDefined) {
											println(s"State ${
												stateId.get
											} has a different ISO-code from proposed $stateIsoCode")
											countryId -> stateId
										}
										// Case: Country found but state not => inserts a new state
										else {
											row.stateName match {
												case Some(stateName) =>
													val newStateId = StateModel.insert(StateData(stateName,
														countryId, stateIsoCode, row.stateFipsCode)).id
													countryId -> Some(newStateId)
												// Case: State ISO-code provided but no state name provided
												case None =>
													println(s"No state name provided for state $stateIsoCode")
													countryId -> stateId
											}
										}
								}
							case None => findCountryAndStateWithoutStateIsoCode(row)
						}
						
						// Makes sure the world area code exists in the database. Inserts one if necessary.
						if (DbWorldArea(row.cityMarketWorldAreaCode).isEmpty)
							WorldAreaModel.insert(WorldArea(row.cityMarketWorldAreaCode, countryId, stateId))
						
						// Attempts to find an existing city based on the city name
						// Prefers exact match but uses word containment if necessary
						val cityName = row.cityName
						val cityId = existingCityIdsByName.get(cityName.toLowerCase).orElse {
							val cityWords = cityName.words
							existingCityIdsByName.keys.filter { _.containsAll(cityWords) }
								.minByOption { _.length }.map { existingCityIdsByName(_) }
						} match {
							// Case: Pre-existing city found
							case Some(cityId) =>
								// Updates city data in the DB
								CityModel.withId(cityId).withMarketId(row.cityMarketId)
									.withWorldAreaCode(row.cityMarketWorldAreaCode).withStateId(stateId)
									.withTimeZone(row.timeZoneDifference).update()
								cityId
							// Case: New city
							case None =>
								// Inserts the new city to the DB
								CityModel.insert(CityData(cityName, countryId, Some(row.cityMarketId), stateId,
									Some(row.cityMarketWorldAreaCode), row.timeZoneDifference)).id
						}
						// Records city market id -> city id connection
						capturedMarketAndCityIds += row.cityMarketId -> cityId
						cityId
					})
					
					// Prepares an airport to be inserted
					station.StationData(row.airportName, row.airportCoordinates, None, Some(Airport.id),
						iataCode = row.airportIataCode, cityId = Some(cityId), started = Some(row.startDate),
						closed = row.closeDate, isClosed = row.isClosed)
				}
				// Inserts the airports in groups
				.foreachGroup(maxInsertSize) { data =>
					println(s"Importing up to $maxInsertSize airports")
					StationModel.insert(data)
				}
		}
	}
	
	// Returns country id -> state id (optional). None if no connection was found.
	private def findCountryAndStateWithoutStateIsoCode(row: AirportRow)(implicit connection: Connection) =
	{
		row.countryIsoCode match {
			// Case: Country ISO-code provided => Connects to the matching country
			case Some(countryIsoCode) =>
				DbCountry.withIsoCode(countryIsoCode) match {
					// Case: Country existed already => connects to that country
					case Some(country) => country.id -> None
					case None =>
						// Makes sure the country doesn't exist without an ISO-code either
						DbCountry.withName(row.countryName, ignoreCountriesWithIsoCode = true) match {
							// Case: Name match => Updates country ISO code and connects to that country
							case Some(nameMatch) =>
								CountryModel.withId(nameMatch.id).withIsoCode(countryIsoCode).update()
								nameMatch.id -> None
							// Case: No match => inserts a new country
							case None =>
								CountryModel.insert(CountryData(row.countryName,
									Some(row.worldRegionCode), Some(countryIsoCode))).id -> None
						}
				}
			// Case: Can't link country / state with ISO-code => Uses WAC instead
			case None =>
				DbWorldArea(row.cityMarketWorldAreaCode).pull match {
					// Case: Matching world area found => collects country and state ids
					case Some(worldArea) => worldArea.countryId -> worldArea.stateId
					// Case: No matching world area found => Attempts to find country based on name
					case None =>
						DbCountry.withName(row.countryName, preferCountriesWithoutIsoCode = true) match {
							// Case: Country was found with name search => uses that
							case Some(country) => country.id -> None
							// Case: No country found => Inserts a new country
							case None =>
								CountryModel.insert(
									CountryData(row.countryName, Some(row.worldRegionCode))).id -> None
						}
				}
		}
	}
	
	
	// NESTED   ---------------------------
	
	private object AirportRow extends FromModelFactoryWithSchema[AirportRow]
	{
		override val schema = ModelDeclaration("CITY_MARKET_ID" -> IntType, "CITY_MARKET_WAC" -> IntType,
			"DISPLAY_AIRPORT_CITY_NAME_FULL" -> StringType, "LATITUDE" -> DoubleType, "LONGITUDE" -> DoubleType,
			"AIRPORT_START_DATE" -> LocalDateType)
		
		// AIRPORT_IS_LATEST is 0/1, but represents a boolean
		override protected def fromValidatedModel(model: Model[Constant]) =
			AirportRow(model("DISPLAY_AIRPORT_NAME"),
				Coordinates(Angle.ofDegrees(model("LATITUDE")), Angle.ofDegrees(model("LONGITUDE"))),
				model("AIRPORT_START_DATE"),
				model("DISPLAY_AIRPORT_CITY_NAME_FULL"), model("CITY_MARKET_ID"), model("CITY_MARKET_WAC"),
				model("AIRPORT_COUNTRY_NAME"), model("AIRPORT_COUNTRY_CODE_ISO"),
				model("AIRPORT_STATE_NAME"), model("AIRPORT_STATE_CODE"), model("AIRPORT_STATE_FIPS"),
				model("AIRPORT"), model("UTC_LOCAL_TIME_VARIATION"), model("AIRPORT_THRU_DATE"),
				model("AIRPORT_IS_CLOSED").int.exists { _ > 0 }, model("AIRPORT_IS_LATEST").int.exists { _ > 0 })
	}
	
	private case class AirportRow(airportName: String, airportCoordinates: Coordinates, startDate: LocalDate,
	                              fullCityName: String, cityMarketId: Int, cityMarketWorldAreaCode: Int,
	                              countryName: String, countryIsoCode: Option[String] = None,
	                              stateName: Option[String] = None, stateIsoCode: Option[String] = None,
	                              stateFipsCode: Option[Int] = None, airportIataCode: Option[String] = None,
	                              timeZoneVariation: Option[Int] = None, closeDate: Option[LocalDate] = None,
	                              isClosed: Boolean = false, isLatestVersion: Boolean = false)
	{
		def worldRegionCode = cityMarketWorldAreaCode / 100
		
		def cityName = fullCityName.untilFirst(",")
		
		// Time zone variation is given as an integer. E.g. -930 where the last 2 digits indicate minutes and the
		// the digits before those indicate hours. Sign indicates direction
		def timeZoneDifference = timeZoneVariation.map { variation =>
			val numberOfHours = variation / 100
			val numberOfMinutes = variation % 100
			numberOfHours.hours + numberOfMinutes.minutes
		}
	}
	
}
