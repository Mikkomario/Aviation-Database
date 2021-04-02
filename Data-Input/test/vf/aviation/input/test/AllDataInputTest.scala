package vf.aviation.input.test

import utopia.flow.generic.DataType
import utopia.flow.util.FileExtensions._
import utopia.vault.sql.Delete
import vf.aviation.core.database.{AviationTables, ConnectionPool}
import vf.aviation.core.util.Globals._
import vf.aviation.input.controller.ImportWacCountryState

import java.nio.file.Path
import scala.util.{Failure, Success}

/**
 * Reads all input data in order
 * @author Mikko Hilpinen
 * @since 2.4.2021, v0.1
 */
object AllDataInputTest extends App
{
	DataType.setup()
	
	val inputDirectory: Path = "Data-Input/input"
	
	ConnectionPool { implicit connection =>
		println("Deleting old data")
		connection(Delete(AviationTables.worldRegion))
		connection(Delete(AviationTables.country))
		
		println("Starting world area code document processing")
		ImportWacCountryState(inputDirectory/"846163630_T_WAC_COUNTRY_STATE.csv") match
		{
			case Success(_) => println("Finished importing data")
			case Failure(error) => error.printStackTrace()
		}
	}
}
