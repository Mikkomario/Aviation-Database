package vf.aviation.input.test

import utopia.flow.generic.DataType
import utopia.flow.util.FileExtensions._
import utopia.vault.database.Connection
import utopia.vault.sql.Delete
import vf.aviation.core.database.{AviationTables, ConnectionPool}
import vf.aviation.core.util.Globals._
import vf.aviation.input.controller.aircraft.{ImportActRef, ImportBstAircraftManufacturers, ImportOrder7360Manufacturers}

import java.nio.file.Path
import scala.util.{Failure, Success}

/**
 * Reads all aircraft-related input data in order
 * @author Mikko Hilpinen
 * @since 2.4.2021, v0.1
 */
object AllAircraftDataImportTest extends App
{
	DataType.setup()
	// Specifies correct character sets for the database connections
	Connection.modifySettings { _.copy(charsetName = "UTF-8", charsetCollationName = "utf8_general_ci") }
	
	val inputDirectory: Path = "Data-Input/input"
	
	ConnectionPool { implicit connection =>
		// Deletes pre-existing data
		println("Deleting old data")
		connection(Delete(AviationTables.aircraftManufacturer))
		connection(Delete(AviationTables.aircraftModel))
		
		// Imports Manufacturers.csv data
		println("Starting BST Manufacturers document processing")
		ImportBstAircraftManufacturers(inputDirectory/"doc8643Manufacturers.csv")
			.flatMap { _ =>
				println("Staring Order 7360 Manufacturer document processing")
				ImportOrder7360Manufacturers(inputDirectory/"order-7360-manufacturers.csv")
			}
			.flatMap { _ =>
				println("Starting ACFTREF document processing")
				ImportActRef(inputDirectory/"ACFTREF.csv")
			}
		match
		{
			case Success(_) => println("Finished importing data")
			case Failure(error) => error.printStackTrace()
		}
	}
}
