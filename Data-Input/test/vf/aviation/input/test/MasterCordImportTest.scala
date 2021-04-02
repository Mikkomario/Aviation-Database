package vf.aviation.input.test

import utopia.flow.generic.DataType
import utopia.flow.util.FileExtensions._
import vf.aviation.core.database.ConnectionPool
import vf.aviation.core.util.Globals._
import vf.aviation.input.controller.ImportMasterCord

import java.nio.file.Path
import scala.util.{Failure, Success}

/**
 * Imports MASTER_CORD file to the database
 * @author Mikko Hilpinen
 * @since 2.4.2021, v0.1
 */
object MasterCordImportTest extends App
{
	DataType.setup()
	
	val inputDirectory: Path = "Data-Input/input"
	
	ConnectionPool { implicit connection =>
		ImportMasterCord(inputDirectory/"846163630_T_MASTER_CORD.csv") match
		{
			case Success(_) => println("Imported airport data")
			case Failure(error) => error.printStackTrace()
		}
	}
}
