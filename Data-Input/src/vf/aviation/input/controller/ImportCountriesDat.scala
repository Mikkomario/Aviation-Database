package vf.aviation.input.controller

import utopia.flow.parse.CsvReader
import utopia.flow.util.CollectionExtensions._
import utopia.flow.util.StringExtensions._
import utopia.vault.database.Connection
import vf.aviation.core.database.access.many.country.DbCountries
import vf.aviation.core.database.access.single.country.DbCountry
import vf.aviation.core.database.model.country.CountryModel
import vf.aviation.core.model.partial.country.CountryData
import vf.aviation.core.model.stored.country.Country

import java.nio.file.Path
import scala.io.Codec
import scala.util.{Failure, Success}

/**
 * Used for importing countries.dat file as country information.
 * Please note that "Faeroe Islands" should be renamed to "Faroe Islands" in the original data
 * in order to match properly.
 * @author Mikko Hilpinen
 * @since 2.4.2021, v0.1
 */
object ImportCountriesDat
{
	// ATTRIBUTES   ------------------------
	
	private implicit val codec: Codec = Codec.UTF8
	
	private val nullString = "\\N"
	
	
	// OTHER    ----------------------------
	
	/**
	 * Reads country data from the countries.dat file and updates the database accordingly
	 * @param path Path to read
	 * @param separator Separator between columns (default = ",")
	 * @param connection DB Connection (implicit)
	 * @return Inserted countries. May contain a failure
	 */
	def apply(path: Path, separator: String = ",")(implicit connection: Connection) =
	{
		// Collects existing country data for name matching
		val (nonIsoCodeCountries, isoCodeCountries) = DbCountries.all.divideBy { _.isoCode.isDefined }
		val countryIdByIsoCode = isoCodeCountries.map { c => c.isoCode.get.toUpperCase -> c.id }.toMap
		println(s"${isoCodeCountries.size} pre-existing countries with ISO-code, ${nonIsoCodeCountries.size} without")
		
		// Processes each row individually
		CsvReader.iterateRawRowsIn(path, separator) { rowIterator =>
			val countriesToInsert = rowIterator.mapCatching(CountryRow.fromRow) { _.printStackTrace() }.flatMap { row =>
				// Prefers ISO-code matching, if possible
				row.isoCode match
				{
					case Some(isoCode) =>
						countryIdByIsoCode.get(isoCode) match
						{
							// Case: ISO-code match was found => updates dafif code, if possible
							case Some(countryId) =>
								row.dafifCode.foreach { dafifCode => DbCountry(countryId).dafifCode = dafifCode }
								None
							// Case: No ISO-code match was found => attempts to match by name
							case None =>
								matchByName(row.name, nonIsoCodeCountries) match
								{
									// Case: Match was found => ISO code is assigned
									case Some(matchingCountry) =>
										CountryModel.withId(matchingCountry.id).withIsoCode(isoCode)
											.withDafifCode(row.dafifCode).update()
										None
									// Case: No match was found => inserts a new country
									case None => Some(CountryData(row.name, isoCode = Some(isoCode),
										dafifCode = row.dafifCode))
								}
						}
					case None =>
						// Attempts to find matching country by name
						matchByName(row.name, nonIsoCodeCountries ++ isoCodeCountries) match
						{
							// Case: Matching country was found => assigns dafif-code (if present)
							case Some(matchingCountry) =>
								row.dafifCode.foreach { dafifCode =>
									DbCountry(matchingCountry.id).dafifCode = dafifCode
								}
								None
							// Case: No matching country was found => inserts a new country
							case None => Some(CountryData(row.name, isoCode = row.isoCode, dafifCode = row.dafifCode))
						}
				}
			}.toVector
			
			// Inserts new countries as a bulk. Will only insert one country for each ISO-code
			// (in data there is a duplicate row for Palestine)
			val (countryDataWithoutIsoCode, countryDataWithIsoCode) = countriesToInsert.divideBy { _.isoCode.isDefined }
			CountryModel.insert(countryDataWithIsoCode.distinctBy { _.isoCode } ++ countryDataWithoutIsoCode)
		}
	}
	
	private def matchByName(countryName: String, options: Iterable[Country]) =
	{
		val lowerCountryName = countryName.toLowerCase
		// Prefers a direct case-insensitive match, but also uses word-based search if necessary
		options.filter { _.name.toLowerCase.contains(lowerCountryName) }.minByOption { _.name.length }.orElse {
			val words = lowerCountryName.words.filterNot { w => w == "and" }
			options.filter { _.name.toLowerCase.containsAll(words) }.minByOption { _.name.length }
		}
	}
	
	
	// NESTED   ----------------------------
	
	private object CountryRow
	{
		def fromRow(row: Seq[String]) =
		{
			if (row.nonEmpty)
				Success(apply(row.head, row.getOption(1).flatMap(parseCode),
					row.getOption(2).flatMap(parseCode)))
			else
				Failure(new IllegalArgumentException("Country row must contain at leas the country name (index 0)"))
		}
		
		private def parseCode(string: String) =
			if (string.isEmpty || string == nullString) None else Some(string.toUpperCase)
	}
	
	private case class CountryRow(name: String, isoCode: Option[String] = None, dafifCode: Option[String] = None)
}
