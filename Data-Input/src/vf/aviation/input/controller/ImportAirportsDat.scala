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
import vf.aviation.core.database.access.many.country.{DbCities, DbCountries}
import vf.aviation.core.database.access.many.station.{DbStationTypes, DbStations}
import vf.aviation.core.database.access.single.country.{DbCity, DbCountry}
import vf.aviation.core.database.access.single.station.DbStation.DbAirport
import vf.aviation.core.database.model.StationModel
import vf.aviation.core.database.model.country.{CityModel, CountryModel}
import vf.aviation.core.model.cached.Coordinates
import vf.aviation.core.model.enumeration.StandardStationType.{Airport, FerryTerminal, TrainStation}
import vf.aviation.core.model.partial.country.{CityData, CountryData}
import vf.aviation.core.model.partial.station.StationData
import vf.aviation.core.model.stored.country.City

import java.nio.file.Path
import scala.concurrent.duration.Duration
import scala.io.Codec
import scala.util.Failure

/**
 * Imports airports.dat file contents to the database. Expects previous airport-related data to include proper city
 * links. "Myanmar" should be renamed to "Burma" in the original data to match properly. The process will work better
 * if rows are ordered by country name (index 3).
 * @author Mikko Hilpinen
 * @since 2.4.2021, v0.1
 */
object ImportAirportsDat
{
	// ATTRIBUTES   -----------------------
	
	private implicit val codec: Codec = Codec.UTF8
	
	private val nullString = "\\N"
	private val maxInsertSize = 1500
	
	
	// OTHER    ---------------------------
	
	/**
	 * Imports data from airports.dat file
	 * @param path Path to the file to read
	 * @param separator Separator between columns (default = ",")
	 * @param connection Db Connection (implicit)
	 * @return Success or failure
	 */
	def apply(path: Path, separator: String = ",")(implicit connection: Connection) =
	{
		// Reads station types from the database
		val stationTypeIdPerCode = DbStationTypes.all.map { t => t.openFlightsCode -> t.id }.toMap
		// Also reads available daylight saving zone codes
		val daylightSavingZoneCodes = DbDaylightSavingZoneCodes.all.map { _.toUpper }.toSet
		
		CsvReader.iterateRawRowsIn(path, separator) { rowsIterator =>
			rowsIterator.mapCatching(AirportRow.fromRow) { _.printStackTrace() }
				// Ignores airports that are marked as "[Duplicate]"
				.filterNot { _.isDuplicate }
				// Attempts to group rows based on country
				.groupBy { _.countryName }
				// Processes country & city data and prepares station data for bulk insert
				.flatMap { case (countryName, rows) =>
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
					// Updates iata match airports and also the associated cities
					iataMatchAirports.foreach { case (row, airport) =>
						// Expects the previous inserts to include proper city information,
						// Therefore won't add city id if one was missing
						airport.cityId.foreach { cityId =>
							CityModel.withId(cityId).withTimeZoneName(row.timeZoneName)
								.withDaylightSavingZoneCode(
									row.daylightSavingsZone.filter(daylightSavingZoneCodes.contains)).update()
						}
						StationModel.withId(airport.id).withOpenFlightsId(row.id).withIcaoCode(row.icaoCode)
							.withAltitude(row.altitude).update()
					}
					// For the rest of the airport matches / updates, needs country data
					if (nonMatchingAirportRows.isEmpty && nonAirportRows.isEmpty)
						Vector()
					else
					{
						// Finds out which country this data is linked to. Uses IATA-airport links if possible.
						val iataLinkCityIds = iataMatchAirports.flatMap { _._2.cityId }.toSet
						val iataLinkCountryIds =
						{
							if (iataLinkCityIds.isEmpty)
								Set[Int]()
							else
								DbCities.withIds(iataLinkCityIds).countryIds
						}
						val existingCountryId =
						{
							// Case: Iata link country found
							if (iataLinkCountryIds.size == 1)
								Some(iataLinkCountryIds.head)
							// Case: Multiple options found
							else if (iataLinkCountryIds.size > 1)
							{
								val countries = DbCountries(iataLinkCountryIds).all
								val lowerCountryName = countryName.toLowerCase
								countries.find { _.name.toLowerCase == lowerCountryName }
									.orElse {
										val words = lowerCountryName.words
										countries.filter { _.name.toLowerCase.containsAll(words) }
											.minByOption { _.name.length }
									}
									.map { _.id }
							}
							// Case: No Iata match found => Country name search required
							// TODO: Could pull only id instead
							else
								DbCountry.withName(countryName).map { _.id }.orElse {
									// If there weren't any results with country name search either,
									// checks with city names (exact) as the last option
									val existingCities = rows.flatMap { _.cityName }.toSet[String]
										.flatMap { DbCities.withExactName(_) }
									if (existingCities.isEmpty)
										None
									else if (existingCities.size == 1)
										Some(existingCities.head.countryId)
									else
									{
										// In case there are competing options,
										// selects the country with most matching cities
										val availableCountryIds = existingCities.groupBy { _.countryId }
											.view.mapValues { _.size }.toMap
										val mostCitiesCount = availableCountryIds.valuesIterator.max
										
										val competingOptions = availableCountryIds.filter { _._2 == mostCitiesCount }
										// Shows a warning if the options can't be distinguished at this time
										if (competingOptions.size > 1)
											println(s"Warning: Multiple country options (${
												competingOptions.keys.mkString(", ")}) for cities: ${
												rows.flatMap { _.cityName }.mkString(", ") }")
										competingOptions.keys.headOption
									}
								}
						}
						val remainingRowsView = nonMatchingAirportRows.view.map { Some(Airport.id) -> _ } ++
							nonAirportRows.view.flatMap { case (typeId, rows) => rows.map { typeId -> _ } }
						def insertCity(cityName: String, row: AirportRow, countryId: Int) = CityModel.insert(
							CityData(cityName, countryId, timeZone = row.timeZoneDifference,
								timeZoneName = row.timeZoneName, daylightSavingZoneCode = row.daylightSavingsZone
									.filter(daylightSavingZoneCodes.contains)))
						existingCountryId match
						{
							// Case: Existing country found => Targets existing cities in that country, if possible
							case Some(countryId) =>
								// Each remaining row is handled individually
								remainingRowsView.map { case (typeId, row) =>
									// Attempts to find an existing city,
									// except when there is no city name listed in the data
									val city = row.cityName.map { cityName =>
										DbCity.inCountryWithId(countryId).withName(cityName).getOrElse {
											// Inserts new city if necessary
											insertCity(cityName, row, countryId)
										}
									}
									// Prepares a new station for insert
									row.toStationData(city.map { _.id }, typeId)
								}
							// Case: No existing country found => Country (and city) are inserted
							case None =>
								println(s"No match could be found for country name '$countryName'")
								val countryId = CountryModel.insert(CountryData(countryName)).id
								var insertedCities = Vector[City]()
								remainingRowsView.map { case (typeId, row) =>
									// Attempts to reuse cities if they were already inserted.
									// Only inserts city data for rows with city name
									val city = row.cityName.map { cityName =>
										insertedCities.find { _.name == cityName }.getOrElse {
											val newCity = insertCity(cityName, row, countryId)
											insertedCities :+= newCity
											newCity
										}
									}
									// Prepares station for insert
									row.toStationData(city.map { _.id }, typeId)
								}
						}
					}
				}
				// Inserts the new airports / stations in bulks
				.foreachGroup(maxInsertSize) { stationData =>
					println(s"Inserting ${stationData.size} new stations")
					StationModel.insert(stationData)
				}
			
			// After all data has been imported, updates some unspecified type ids based on station name
			println("Assigns types to unspecified stations based on station name")
			// TODO: UpdatedRowCount doesn't work properly
			val stationsAccess = DbStations.withoutType
			val newTrainStationsCount = stationsAccess.updateTypeWithName(TrainStation.id,
				Vector("Railway", "Train Station", "Train Depot"))
			val newAirportsCount = stationsAccess.updateTypeWithName(Airport.id,
				Vector("Airport", "Airfield", "Airstrip", "Air Base", "Air Force Base",
				"Airpark", "Air Park", "Aerop", "Aeroclub", "Aerodrom", "Aero Park"))
			val newFerryPortsCount = stationsAccess.updateTypeWithName(FerryTerminal.id, Vector("Ferry"))
			
			println(s"Assigned $newAirportsCount stations as airports, $newTrainStationsCount as railway stations and $newFerryPortsCount as ferry ports")
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
				/*
					- 0: Airport id
					- 1: Name
					- 2: City Name
					- 3: Country Name
					- 4: IATA Code
					- 5: ICAO Code
					- 6: Latitude
					- 7: Longitude
					- 8: Altitude
					- 9: Time Zone (Diff)
					- 10: Daylight Saving Zone Code
					- 11: Time Zone Name
					- 12: Type Code
				 */
				def value(index: Int): Value = row(index).notEmpty.filter { _ != nullString }
				value(6).double.toTry { new IllegalArgumentException(s"Latitude ${row(6)} is invalid") }
					.flatMap { latitude =>
						value(7).double.toTry { new IllegalArgumentException(s"Longitude ${row(7)} is invalid") }
							.map { longitude =>
								AirportRow(value(0).getInt, row(1), row(3),
									Coordinates(Angle.ofDegrees(latitude), Angle.ofDegrees(longitude)),
									value(8).double.map(Distance.ofFeet),
									value(4).string, value(5).string, value(2).string,
									value(11).string, value(9).double.map { _.hours },
									value(10).string.flatMap { _.toUpperCase.headOption },
									value(12).string)
							}
					}
			}
		}
	}
	
	private case class AirportRow(id: Int, name: String, countryName: String, coordinates: Coordinates,
	                              altitude: Option[Distance] = None, iataCode: Option[String] = None,
	                              icaoCode: Option[String] = None, cityName: Option[String] = None,
	                              timeZoneName: Option[String] = None, timeZoneDifference: Option[Duration] = None,
	                              daylightSavingsZone: Option[Char] = None, typeCode: Option[String] = None)
	{
		/**
		 * @return Whether this row represents duplicate data and should be ignored
		 */
		def isDuplicate = name.contains("[Duplicate]")
		
		def toStationData(cityId: Option[Int], typeId: Option[Int]) = StationData(name, coordinates, altitude, typeId,
			openFlightsId = Some(id), iataCode = iataCode, icaoCode = icaoCode, cityId = cityId)
	}
}
