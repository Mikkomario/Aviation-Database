package vf.aviation.input.test

import utopia.flow.generic.DataType
import utopia.flow.util.FileExtensions._
import utopia.vault.database.Connection
import vf.aviation.core.database.ConnectionPool
import vf.aviation.core.util.Globals._
import vf.aviation.input.controller.carrier

import java.nio.file.Path
import scala.util.{Failure, Success}

/**
 * Imports the contents of route mapper -originated airlines.dat file
 * @author Mikko Hilpinen
 * @since 4.4.2021, v0.1
 */
object RouteMapperAirlinesImportTest extends App
{
	DataType.setup()
	// Specifies correct character sets for the database connections
	Connection.modifySettings { _.copy(charsetName = "UTF-8", charsetCollationName = "utf8_general_ci") }
	
	val inputDirectory: Path = "Data-Input/input"
	
	ConnectionPool { implicit connection =>
		carrier.ImportRouteMapperAirlines(inputDirectory/"route-mapper-airlines.csv") match
		{
			case Success(data) => println(s"Inserted ${data.size} new airlines to the DB")
			case Failure(error) => error.printStackTrace()
		}
	}
}
