package vf.aviation.input.controller.aircraft

import utopia.flow.datastructure.immutable.{Constant, Model}
import utopia.flow.util.CollectionExtensions._
import utopia.flow.generic.FromModelFactoryWithSchema
import utopia.flow.parse.CsvReader
import utopia.flow.util.ActionBuffer
import utopia.vault.database.Connection
import vf.aviation.core.database.access.many.aircraft.DbAircraftManufacturers
import vf.aviation.core.database.access.single.aircraft.DbAircraftManufacturer
import vf.aviation.core.database.model.aircraft.{AircraftManufacturerModel, AircraftManufacturerNameModel}
import vf.aviation.core.model.combined.FullAircraftManufacturer
import vf.aviation.core.model.partial.aircraft.{AircraftManufacturerData, AircraftManufacturerNameData}

import java.nio.file.Path

/**
 * Imports aircraft model, variant and manufacturer data from the ACTREF.txt file.
 * Expects aircraft manufacturers to be registered already, but no aircraft models to be present yet.
 * @author Mikko Hilpinen
 * @since 5.4.2021, v0.1
 */
object ImportActRef
{
	// ATTRIBUTES   --------------------------
	
	private val maxInsertSize = 1000
	
	
	// OTHER    ------------------------------
	
	def apply(path: Path, separator: String = ",")(implicit connection: Connection) =
	{
		CsvReader.iterateLinesIn(path, separator, ignoreEmptyStringValues = true) { linesIterator =>
			// Manufacturer name inserts are made in bulks
			val nameInsertBuffer = ActionBuffer[(Int, String)](maxInsertSize) { items =>
				// Inserts the names
				AircraftManufacturerNameModel.insert(items.map { case (manufacturerId, name) =>
					AircraftManufacturerNameData(manufacturerId, name) })
			}
			
			// Function for adding new names for existing manufacturer. Checks for duplicates.
			def insertUniqueNames(manufacturerId: Int, proposedNames: Seq[String]) =
			{
				val existingLowerCaseNames = DbAircraftManufacturer(manufacturerId).names.map { _.toLowerCase }
				nameInsertBuffer ++= proposedNames
					.filterNot { name => existingLowerCaseNames.contains(name.toLowerCase) }
					.map { manufacturerId -> _ }
			}
			
			linesIterator.mapCatching(VariantRow.apply) { _.printStackTrace() }
				// Groups the rows by manufacturer
				.groupBy { _.manufacturerCode }
				.foreach { case (manufacturerCode, rows) =>
					val manufacturerNames = rows.map { _.manufacturerName }.toSet
					// Finds manufacturer ids matching those names (may contain duplicates)
					val matchingManufacturers = manufacturerNames
						.flatMap { name => DbAircraftManufacturer.forName(name).map { name -> _ } }.toMap
					// In case there are multiple options found, prefers those that don't have an alt-code assigned
					// yet (because they are already associated with a different row group while not having the same code)
					val distinctManufacturers = matchingManufacturers.valuesIterator.toSet
						.bestMatch(Vector(m => m.alternativeCode.isEmpty))
					
					// Assigns the rows between the manufacturer(s)
					val rowsPerManufacturer =
					{
						// Case: No matching manufacturers found
						if (distinctManufacturers.isEmpty)
						{
							// Inserts a new manufacturer to the DB.
							val manufacturer = AircraftManufacturerModel.insert(
								AircraftManufacturerData(alternativeCode = Some(manufacturerCode)))
							// Inserts all the names later.
							nameInsertBuffer ++= manufacturerNames.toVector.map { manufacturer.id -> _ }
							
							// Uses this manufacturer for all the rows
							Map(manufacturer -> rows)
						}
						// Case: Unique manufacturer found
						else if (distinctManufacturers.size == 1)
						{
							val manufacturer = distinctManufacturers.head
							// Adds the alt-code for that manufacturer
							DbAircraftManufacturer(manufacturer.id).altCode = manufacturerCode
							// Assigns new names for that manufacturer (no duplicates, however)
							insertUniqueNames(manufacturer.id, manufacturerNames.toVector)
							// Uses that manufacturer for all rows
							Map(manufacturer -> rows)
						}
						// Case: Multiple manufacturers found
						else
						{
							// Assigns the alt-code for those manufacturers
							DbAircraftManufacturers(distinctManufacturers.map { _.id })
								.assignAlternativeCodeIfNotSet(manufacturerCode)
							
							// Case: All names were successful assigned
							if (manufacturerNames.size == matchingManufacturers.size)
							{
								// Inserts new names for the manufacturers based on assignments, but avoids duplicates
								val namesByManufacturers = matchingManufacturers.toVector.groupMap { _._2 } { _._1 }
								namesByManufacturers.foreach { case (manufacturer, names) =>
									insertUniqueNames(manufacturer.id, names)
								}
								
								// Assigns the rows based on manufacturer names
								namesByManufacturers.view.mapValues { names =>
									rows.filter { row => names.contains(row.manufacturerName) } }.toMap
							}
							// Case: Some names couldn't be assigned
							else
							{
								// Assigns the names to the manufacturer with most resemblance, or to the most
								// prominent manufacturer
								val unassignedNames = manufacturerNames -- matchingManufacturers.keySet
								val manufacturersWithNames = distinctManufacturers
									.map { m => DbAircraftManufacturer(m.id).full
										.getOrElse { FullAircraftManufacturer(m, Vector()) } }
								// TODO: Continue
								???
							}
						}
					}
				}
		}
	}
	
	
	// NESTED   ------------------------------
	
	private object VariantRow extends FromModelFactoryWithSchema[VariantRow]
	{
		override val schema = ???
		
		override protected def fromValidatedModel(model: Model[Constant]) = ???
	}
	
	private case class VariantRow(code: String, manufacturerName: String)
	{
		// First 3 characters of the code denote a manufacturer
		def manufacturerCode = code.take(3)
		// The 4th and 5th show aircraft model
		def modelCode = code.slice(3, 5)
		// The 6th and 7th describe model variant / series
		def variantCode = code.drop(5)
	}
}
