package vf.aviation.input.test

import utopia.flow.generic.DataType
import utopia.flow.util.FileExtensions._
import utopia.vault.database.Connection
import vf.aviation.core.database.ConnectionPool
import vf.aviation.core.util.Globals._
import vf.aviation.input.controller.aircraft.ImportOrder7360Manufacturers

import java.nio.file.Path
import scala.util.{Failure, Success}

/**
 * Imports the contents of route mapper -originated airlines.dat file
 * @author Mikko Hilpinen
 * @since 4.4.2021, v0.1
 */
object Order7360ManufacturersImportTest extends App
{
	DataType.setup()
	// Specifies correct character sets for the database connections
	Connection.modifySettings { _.copy(charsetName = "UTF-8", charsetCollationName = "utf8_general_ci") }
	
	val inputDirectory: Path = "Data-Input/input"
	
	ConnectionPool { implicit connection =>
		ImportOrder7360Manufacturers(inputDirectory/"order-7360-manufacturers.csv") match
		{
			case Success(_) => println("Done!")
			case Failure(error) => error.printStackTrace()
		}
	}
}
