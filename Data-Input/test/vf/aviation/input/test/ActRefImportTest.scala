package vf.aviation.input.test

import utopia.flow.generic.DataType
import utopia.flow.parse.CsvReader
import utopia.flow.util.FileExtensions._
import utopia.vault.database.Connection
import vf.aviation.core.database.ConnectionPool
import vf.aviation.core.util.Globals._
import vf.aviation.input.controller.aircraft.ImportActRef

import java.nio.file.Path
import scala.util.{Failure, Success}

/**
 * Imports the contents of ACTREF file (aircraft models)
 * @author Mikko Hilpinen
 * @since 4.4.2021, v0.1
 */
object ActRefImportTest extends App
{
	DataType.setup()
	// Specifies correct character sets for the database connections
	Connection.modifySettings { _.copy(charsetName = "UTF-8", charsetCollationName = "utf8_general_ci") }
	
	val inputDirectory: Path = "Data-Input/input"
	val path = inputDirectory/"ACFTREF.csv"
	
	/*
	println("Raw rows (3)")
	CsvReader.iterateRawRowsIn(path, ",") { _.take(3).foreach { row =>
		println(row.mkString(", "))
	} }
	println("Parsed rows (3)")
	CsvReader.iterateLinesIn(path, ",") { _.take(3).foreach { row =>
		println(row.toJson)
		println(row.attributes.map { c => s"${c.name}: ${c.value.description}" }.mkString(", "))
	} }*/
	
	ConnectionPool { implicit connection =>
		ImportActRef(path) match
		{
			case Success(_) => println("Done!")
			case Failure(error) => error.printStackTrace()
		}
	}
}
