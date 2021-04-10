package vf.aviation.input.controller.aircraft

import utopia.flow.datastructure.immutable.{Constant, Model, ModelDeclaration}
import utopia.flow.util.CollectionExtensions._
import utopia.flow.generic.{FromModelFactoryWithSchema, IntType, StringType}
import utopia.flow.generic.ValueUnwraps._
import utopia.flow.parse.{CsvReader, Regex}
import utopia.flow.util.ActionBuffer
import utopia.flow.util.StringExtensions._
import utopia.vault.database.Connection
import vf.aviation.core.database.access.many.aircraft.DbAircraftManufacturers
import vf.aviation.core.database.access.single.aircraft.DbAircraftManufacturer
import vf.aviation.core.database.model.aircraft.{AircraftManufacturerModel, AircraftManufacturerNameModel, AircraftModelModel, AircraftModelVariantModel}
import vf.aviation.core.model.cached.Speed
import vf.aviation.core.model.combined.FullAircraftManufacturer
import vf.aviation.core.model.enumeration.StandardAircraftCategory.{Airplane, Airship, Balloon, Gyroplane, Helicopter, HybridAirship, NonPoweredGlider, PoweredGlider}
import vf.aviation.core.model.enumeration.StandardAircraftWeightCategory.{Light, LightPlus, Medium, MediumPlus, Super}
import vf.aviation.core.model.enumeration.StandardGenericEngineType.Piston
import vf.aviation.core.model.enumeration.StandardSpecificEngineType.{Electric, FourCycle, RamJet, Rotary, TurboFan, TurboJet, TurboProp, TurboShaft, TwoCycle}
import vf.aviation.core.model.partial.aircraft.{AircraftManufacturerData, AircraftManufacturerNameData, AircraftModelData, AircraftModelVariantData}
import vf.aviation.core.model.stored.aircraft.AircraftManufacturer

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
	
	private val maxInsertSize = 500
	
	
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
				.flatMap { case (manufacturerCode, rows) =>
					val manufacturerNames = rows.map { _.manufacturerName }.toSet
					// Finds manufacturer ids matching those names (may contain duplicates)
					val matchingManufacturers = manufacturerNames
						.flatMap { name => DbAircraftManufacturer.forName(name).map { name -> _ } }.toMap
					// In case there are multiple options found, prefers those that don't have an alt-code assigned
					// yet (because they are already associated with a different row group while not having the same code)
					val distinctManufacturers = matchingManufacturers.valuesIterator.toSet
						.bestMatch(Vector(m => m.alternativeCode.isEmpty))
					
					// Assigns the rows between the manufacturer(s)
					val rowsWithManufacturer: Vector[(VariantRow, AircraftManufacturer)] =
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
							rows.map { _ -> manufacturer }
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
							rows.map { _ -> manufacturer }
						}
						// Case: Multiple manufacturers found
						else
						{
							// Assigns the alt-code for those manufacturers
							DbAircraftManufacturers(distinctManufacturers.map { _.id })
								.assignAlternativeCodeIfNotSet(manufacturerCode)
							
							// Inserts new names for the manufacturers based on assignments, but avoids duplicates
							val namesByManufacturers = matchingManufacturers.toVector.groupMap { _._2 } { _._1 }
							namesByManufacturers.foreach { case (manufacturer, names) =>
								insertUniqueNames(manufacturer.id, names)
							}
							
							// Case: All names were successful assigned
							if (manufacturerNames.size == matchingManufacturers.size)
							{
								// Assigns the rows based on manufacturer names
								rows.map { row => row -> matchingManufacturers(row.manufacturerName) }
							}
							// Case: Some names couldn't be assigned
							else
							{
								// Assigns the names to the manufacturer with most resemblance
								val unassignedNames = manufacturerNames -- matchingManufacturers.keySet
								val manufacturersWithNames = distinctManufacturers
									.map { m => DbAircraftManufacturer(m.id).full
										.getOrElse { FullAircraftManufacturer(m, Vector()) } }
								val chosenManufacturers = unassignedNames
									.map { name => name -> findBestMatch(name, manufacturersWithNames) }.toMap
								
								// Inserts new names for those manufacturers also
								// Avoids duplicates, even though there shouldn't be any at this point
								nameInsertBuffer ++= chosenManufacturers.flatMap { case (name, manufacturer) =>
									if (manufacturer.names.exists { _ ~== name })
										None
									else
										Some(manufacturer.id -> name)
								}.toSeq
								
								val allMatchesMap = matchingManufacturers ++
									chosenManufacturers.view.mapValues { _.manufacturer }
								rows.map { row => row -> allMatchesMap(row.manufacturerName) }
							}
						}
					}
					
					// Handles each model code separately
					rowsWithManufacturer.groupBy { _._1.modelCode }.flatMap { case (modelCode, rows) =>
						// Groups the rows based on A/C category information
						rows.groupBy { case (row, _) => (row.cateGoryId, row.typeId, row.engineTypeId,
							row.numberOfEngines, row.weightClassId) }
							.map { case ((categoryId, typeId, engineTypeId, numberOfEngines, weightClassId), rows) =>
								// Creates a new model row for this group and a single model variant row for each row
								// The inserts themselves are delayed
								// Parses min and max weight categories based on document enumeration (see ardata.pdf)
								// TODO: Handle cases where id is none of these (if present)
								val minWeightCategory = weightClassId match
								{
									case 1 | 4 => Light
									case 2 => LightPlus
									case 3 => MediumPlus
								}
								val maxWeightCategory = weightClassId match
								{
									case 1 | 4 => Light
									case 2 => Medium
									case 3 => Super
								}
								// Parses aircraft category from document enumeration (see ardata.pdf for details)
								val category = typeId match
								{
									case "1" | "7" => Some(if (numberOfEngines > 0) PoweredGlider else NonPoweredGlider)
									case "2" => Some(Balloon)
									case "3" => Some(Airship)
									case "4" | "5" => Some(Airplane)
									case "6" => Some(Helicopter)
									case "8" => Some(PoweredGlider)
									case "9" => Some(Gyroplane)
									case "H" => Some(HybridAirship)
									case _ => None
								}
								// Parses engine type based on document enumeration (see ardata)
								val specificEngineType = engineTypeId match
								{
									case 2 => Some(TurboProp)
									case 3 => Some(TurboShaft)
									case 4 => Some(TurboJet)
									case 5 => Some(TurboFan)
									case 6 => Some(RamJet)
									case 7 => Some(TwoCycle)
									case 8 => Some(FourCycle)
									case 10 => Some(Electric)
									case 11 => Some(Rotary)
									case _ => None
								}
								val genericEngineType = specificEngineType.map { _.genericType }.orElse {
									engineTypeId match
									{
										case 1 => Some(Piston)
										case _ => None
									}
								}
								val modelData = AircraftModelData(categoryId, minWeightCategory.id, maxWeightCategory.id,
									Some(manufacturerCode + modelCode), None, None, category.map { _.id },
									category.map { _.wingType.id }, Some(numberOfEngines),
									genericEngineType.map { _.id }, specificEngineType.map { _.id })
								val variantRows = rows.map { case (row, manufacturer) =>
									VariantInsertInfo(manufacturer.id, row.name, row.code, row.numberOfSeats,
										row.cruisingSpeed)
								}
								modelData -> variantRows
							}
					}
				}
				// Inserts new model & model variant data in bulks
				.foreachGroup(maxInsertSize) { rows =>
					// First inserts the models to acquire model ids
					val models = AircraftModelModel.insert(rows.map { _._1 })
					// Then inserts the model variants
					val variantData = rows.zip(models).flatMap { case ((_, variantRows), model) =>
						variantRows.map { row =>
							AircraftModelVariantData(model.id, row.manufacturerId, row.name, Some(row.code),
								numberOfSeats = Some(row.numberOfSeats), cruisingSpeed = row.cruisingSpeed)
						}
					}
					AircraftModelVariantModel.insert(variantData)
				}
		}
	}
	
	private def findBestMatch(name: String, manufacturers: Iterable[FullAircraftManufacturer]) =
	{
		// Counts the occurrence of words in the name in each manufacturer name
		val nameWords = name.words.map { _.toLowerCase }
		val firstResults = countWordsMatch(nameWords, manufacturers)
		
		if (firstResults.size > 1)
		{
			// If there are still many options, counts the occurrence of word parts in each manufacturer name
			val splitRegex = !Regex.alphaNumeric
			val wordParts = nameWords.flatMap { splitRegex.split(_).filter { _.nonEmpty } }
			val secondResults = countWordsMatch(wordParts, manufacturers)
			
			if (secondResults.size > 1)
			{
				// If there are still many options, counts the number of even shorter word pieces
				(3 to 1).findMap { rangeLength =>
					val wordPieces = wordParts.flatMap { _.splitToSegments(rangeLength) }
					val results = countWordsMatch(wordPieces, manufacturers)
					if (results.size > 1)
						None
					else
						Some(results.head)
				// And if, for some strange reason, there are still many options, simply picks one
				}.getOrElse(secondResults.minBy { _.names.map { _.length }.sum })
			}
			else
				secondResults.head
		}
		else
			firstResults.head
	}
	
	private def countWordsMatch(words: Iterable[String], manufacturers: Iterable[FullAircraftManufacturer]) =
	{
		val counts = manufacturers.map { manufacturer =>
			manufacturer -> manufacturer.names.map { name =>
				val lowerName = name.toLowerCase
				words.count(lowerName.contains)
			}.maxOption.getOrElse(0)
		}
		val maxCount = counts.map { _._2 }.max
		counts.filter { _._2 == maxCount }.map { _._1 }
	}
	
	
	// NESTED   ------------------------------
	
	private case class VariantInsertInfo(manufacturerId: Int, name: String, code: String, numberOfSeats: Int,
	                                     cruisingSpeed: Option[Speed])
	
	private object VariantRow extends FromModelFactoryWithSchema[VariantRow]
	{
		override val schema = ModelDeclaration("MODEL" -> StringType, "CODE" -> StringType, "MRF" -> StringType,
			"TYPE-ACFT" -> IntType,
			"TYPE-ENG" -> IntType, "AC-CAT" -> IntType, "NO-ENG" -> IntType, "AC-WEIGHT" -> IntType,
			"NO-SEATS" -> IntType)
		
		// Weight class is given as "CLASS 1 | CLASS 2 etc."
		// TODO: Add weight class id parsing failure handling
		override protected def fromValidatedModel(model: Model[Constant]) =
			VariantRow(model("NAME"), model("CODE"), model("MFR"), model("AC-CAT"), model("TYPE-ACFT"),
				model("AC-WEIGHT").getString.last.asDigit,
				model("TYPE-ENG"), model("NO-ENG"), model("NO-SEATS"),
				model("SPEED").int.filter { _ > 0 }.map { Speed.milesPerHour(_) })
	}
	
	private case class VariantRow(name: String, code: String, manufacturerName: String, cateGoryId: Int,
	                              typeId: String, weightClassId: Int, engineTypeId: Int, numberOfEngines: Int,
	                              numberOfSeats: Int, cruisingSpeed: Option[Speed] = None)
	{
		// First 3 characters of the code denote a manufacturer
		def manufacturerCode = code.take(3)
		// The 4th and 5th show aircraft model
		def modelCode = code.slice(3, 5)
	}
}
