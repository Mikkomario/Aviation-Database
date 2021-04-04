package vf.aviation.input.test

import utopia.flow.generic.DataType
import utopia.flow.util.FileExtensions._
import utopia.vault.database.Connection
import vf.aviation.core.database.ConnectionPool
import vf.aviation.input.controller.ImportCarrierDecode
import vf.aviation.core.util.Globals._

import java.nio.file.Path
import scala.util.{Failure, Success}

/**
 * Imports data from CARRIER DECODE document
 * @author Mikko Hilpinen
 * @since 4.4.2021, v0.1
 */
object CarrierDecodeImportTest extends App
{
	DataType.setup()
	// Specifies correct character sets for the database connections
	Connection.modifySettings { _.copy(charsetName = "UTF-8", charsetCollationName = "utf8_general_ci") }
	
	val inputDirectory: Path = "Data-Input/input"
	
	ConnectionPool { implicit connection =>
		ImportCarrierDecode(inputDirectory/"846163630_T_CARRIER_DECODE.csv") match
		{
			case Success(_) => println("Done")
			case Failure(error) => error.printStackTrace()
		}
	}
}
