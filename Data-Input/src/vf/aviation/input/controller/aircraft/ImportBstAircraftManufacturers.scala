package vf.aviation.input.controller.aircraft

import utopia.flow.datastructure.immutable.{Constant, Model, ModelDeclaration, PropertyDeclaration}
import utopia.flow.generic.{FromModelFactoryWithSchema, StringType}
import utopia.flow.generic.ValueUnwraps._
import utopia.flow.parse.CsvReader
import utopia.flow.util.CollectionExtensions._
import utopia.flow.util.StringExtensions._
import utopia.vault.database.Connection
import vf.aviation.core.database.access.single.country.DbCountry
import vf.aviation.core.database.model.aircraft.{AircraftManufacturerModel, AircraftManufacturerNameModel}
import vf.aviation.core.model.partial.aircraft.{AircraftManufacturerData, AircraftManufacturerNameData}

import java.nio.file.Path
import scala.collection.mutable

/**
 * Imports aircraft manufacturers listed in BST Manufacturers.csv document
 * @author Mikko Hilpinen
 * @since 4.4.2021, v0.1
 */
object ImportBstAircraftManufacturers
{
	// ATTRIBUTES   ------------------
	
	private val groupSize = 500
	
	
	// OTHER    ----------------------
	
	/**
	 * Reads aircraft manufacturer data from BST Manufacturers.csv. Expects country data to be imported already.
	 * @param path Path to read
	 * @param separator Separator between columns (default = ",")
	 * @param connection DB Connection (implicit)
	 * @return Success for failure
	 */
	def apply(path: Path, separator: String = ",")(implicit connection: Connection) =
	{
		CsvReader.iterateLinesIn(path, separator, ignoreEmptyStringValues = true) { linesIterator =>
			// Minimizes country queries by caching results
			val cachedCountryResults = mutable.Map[String, Option[Int]]()
			def countryIdForName(countryName: String) = cachedCountryResults.getOrElseUpdate(countryName, {
				DbCountry.withName(countryName).map { _.id }
			})
			
			linesIterator.mapCatching(ManufacturerRow.apply) { _.printStackTrace() }
				// Handles the rows in blocks
				.foreachGroup(groupSize) { rows =>
					// Prepares data for insert
					val manufacturers = AircraftManufacturerModel.insert(
						rows.map { row => AircraftManufacturerData(Some(row.icaoCode),
							countryId = row.countryNames.findMap(countryIdForName)) })
					// After insert, applies names
					AircraftManufacturerNameModel.insert(
						manufacturers.zip(rows).flatMap { case (manufacturer, row) =>
							row.name.map { AircraftManufacturerNameData(manufacturer.id, _) }
						})
				}
		}
	}
	
	
	// NESTED   ----------------------
	
	private object ManufacturerRow extends FromModelFactoryWithSchema[ManufacturerRow]
	{
		override val schema = ModelDeclaration(PropertyDeclaration("Code", StringType))
		
		override protected def fromValidatedModel(model: Model[Constant]) =
		{
			val (name, countries) = model("Name").string.filterNot { _.startsWithIgnoreCase("see") } match
			{
				case Some(original) =>
					val (name, countries) = ManufacturerUtils.separateNameAndCountries(original)
					Some(name) -> countries
				case None => None -> Vector()
			}
			ManufacturerRow(model("Code"), name, countries)
		}
	}
	
	private case class ManufacturerRow(icaoCode: String, name: Option[String], countryNames: Vector[String])
}
