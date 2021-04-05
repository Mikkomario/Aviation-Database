package vf.aviation.input.controller.aircraft

import utopia.flow.datastructure.template
import utopia.flow.datastructure.template.Property
import utopia.flow.parse.CsvReader
import utopia.flow.util.CollectionExtensions._
import utopia.flow.util.StringExtensions._
import utopia.vault.database.Connection
import vf.aviation.core.database.access.single.aircraft.DbAircraftManufacturer
import vf.aviation.core.database.access.single.country.DbCountry
import vf.aviation.core.database.model.aircraft.{AircraftManufacturerModel, AircraftManufacturerNameModel}
import vf.aviation.core.model.partial.aircraft.AircraftManufacturerData

import java.nio.file.Path
import scala.collection.immutable.VectorBuilder
import scala.util.Try

/**
 * Imports US Department of Transportation Order 7360 -originated aircraft manufacturers (pdf)
 * from a processed csv file (using Tabula). The conversion from pdf to csv is not perfect and results in some
 * rows missing a manufacturer code. Those are attached to the closest manufacturer available.
 * @author Mikko Hilpinen
 * @since 4.4.2021, v0.1
 */
object ImportOrder7360Manufacturers
{
	// ATTRIBUTES   --------------------
	
	private val maxInsertSize = 500
	
	
	// OTHER    ------------------------
	
	/**
	 * Reads manufacturer data from the specified path
	 * @param path Path to the file to read
	 * @param separator Separator between columns (default = ";")
	 * @param connection DB Connection (implicit)
	 * @return Success or failure
	 */
	def apply(path: Path, separator: String = ";")(implicit connection: Connection): Try[Unit] =
	{
		CsvReader.iterateLinesIn(path, separator, ignoreEmptyStringValues = true) { linesIterator =>
			// Manufacturer id -> name
			val nameInsertsBuilder = new VectorBuilder[(Int, String)]()
			
			// Processes one manufacturer at a time
			// Ignores codes which only consist of digits (original document may be misread)
			new ManufacturerIterator(linesIterator).filterNot { _.code.forall { _.isDigit } }.flatMap { row =>
				// Checks whether the manufacturer already exists in the DB
				DbAircraftManufacturer.withIcaoCode(row.code).full match
				{
					// Case: Already exists => Checks whether names or country data can be inserted
					case Some(existing) =>
						// Updates country link, if necessary & possible
						if (existing.countryId.isEmpty)
							row.countryNames.findMap { DbCountry.withName(_) }.foreach { country =>
								DbAircraftManufacturer(existing.id).countryId = country.id
							}
						// Prepares new name inserts (Ignores those starting with "See")
						nameInsertsBuilder ++= row.names.filterNot { _.toLowerCase.startsWith("see") }
							.filterNot { name => existing.names.exists { _ ~== name } }.map { existing.id -> _ }
						None
					// Case: New manufacturer
					case None =>
						// Acquires country id, if possible
						val countryId = row.countryNames.findMap { DbCountry.withName(_) }.map { _.id }
						val data = AircraftManufacturerData(Some(row.code), countryId = countryId)
						// Inserts names only if country names have been specified
						// (otherwise expects a name parsing failure)
						Some(if (row.countryNames.nonEmpty) data -> row.names else data -> Vector())
				}
			// Performs the inserts in bulks
			}.foreachGroup(maxInsertSize) { data =>
				// Inserts the manufacturers first
				val manufacturers = AircraftManufacturerModel.insert(data.map { _._1 })
				// Then inserts the manufacturer names
				AircraftManufacturerNameModel.insert(manufacturers.zip(data)
					.flatMap { case (manufacturer, (_, names)) =>
						names.map { manufacturer.id -> _ }
					})
			}
			
			// Finally inserts additional name updates
			AircraftManufacturerNameModel.insert(nameInsertsBuilder.result())
		}
	}
	
	
	// NESTED   ------------------------
	
	private case class ManufacturerRow(code: String, names: Vector[String], countryNames: Vector[String])
	
	private class ManufacturerIterator(source: Iterator[template.Model[Property]]) extends Iterator[ManufacturerRow]
	{
		// ATTRIBUTES   ------------------------
		
		private val _source = source.pollable
		
		
		// IMPLEMENTED  ------------------------
		
		override def hasNext = _source.hasNext
		
		override def next() =
		{
			// Case: Single row only
			if (_source.poll.containsNonEmpty("Code"))
			{
				val row = _source.next()
				val (names, countryNames) = namesAndCountryNamesFrom(row)
				ManufacturerRow(row("Code").getString, names, countryNames)
			}
			// Case: Multiple rows, middle of which contains the code
			else
			{
				val firstEmpty = _source.takeNextWhile { _("Code").isEmpty }
				val mainRow = _source.next()
				// Expects an equal amount of empty items above and below the main row
				val remaining = _source.takeNext(firstEmpty.size)
				// Makes sure it is so
				if (remaining.exists { _.containsNonEmpty("Code") })
					println(s"${mainRow("Code").getString} accidentally included ${
						remaining.findMap { _("Code").string }.get }")
				
				// Combines the rows
				val namesBuilder = new VectorBuilder[String]()
				val countryNamesBuilder = new VectorBuilder[String]()
				((firstEmpty :+ mainRow) ++ remaining).foreach { row =>
					val (names, countryNames) = namesAndCountryNamesFrom(row)
					namesBuilder ++= names
					countryNamesBuilder ++= countryNames
				}
				ManufacturerRow(mainRow("Code").getString, namesBuilder.result(), countryNamesBuilder.result())
			}
		}
		
		
		// OTHER    --------------------------
		
		private def namesAndCountryNamesFrom(model: template.Model[Property]) =
		{
			model("Name").string match
			{
				// Case: Name or names
				case Some(defined) =>
					// There may be multiple name "rows" within the name
					val (names, countryNames) = defined.divideWith(")")
						.splitMap(ManufacturerUtils.separateNameAndCountries)
					names.map { _.trim }.filter { _.nonEmpty } -> countryNames.flatten
				// Case: No name
				case None => Vector() -> Vector()
			}
		}
	}
}
