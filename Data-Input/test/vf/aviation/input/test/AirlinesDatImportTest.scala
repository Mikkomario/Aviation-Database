package vf.aviation.input.test

import utopia.flow.generic.DataType
import utopia.vault.database.Connection
import vf.aviation.core.database.ConnectionPool

import java.nio.file.Path
import scala.util.{Failure, Success}
import utopia.flow.util.FileExtensions._
import vf.aviation.core.util.Globals._
import vf.aviation.input.controller.ImportAirlinesDat

/**
 * Imports the contents of airlines.dat file
 * @author Mikko Hilpinen
 * @since 4.4.2021, v0.1
 */
object AirlinesDatImportTest extends App
{
	DataType.setup()
	// Specifies correct character sets for the database connections
	Connection.modifySettings { _.copy(charsetName = "UTF-8", charsetCollationName = "utf8_general_ci") }
	
	val inputDirectory: Path = "Data-Input/input"
	
	ConnectionPool { implicit connection =>
		ImportAirlinesDat(inputDirectory/"airlines.csv") match
		{
			case Success(_) => println("Done")
			case Failure(error) => error.printStackTrace()
		}
	}
}
