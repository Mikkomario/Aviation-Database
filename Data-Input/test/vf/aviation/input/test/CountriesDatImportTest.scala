package vf.aviation.input.test

import utopia.flow.generic.DataType
import utopia.flow.util.FileExtensions._
import vf.aviation.core.database.ConnectionPool
import vf.aviation.core.util.Globals._
import vf.aviation.input.controller.ImportCountriesDat

import java.nio.file.Path
import scala.util.{Failure, Success}

/**
 * Attempts to import the countries.dat file
 * @author Mikko Hilpinen
 * @since 2.4.2021, v0.1
 */
object CountriesDatImportTest extends App
{
	DataType.setup()
	
	val inputDirectory: Path = "Data-Input/input"
	
	ConnectionPool { implicit connection =>
		println("Importing countries.dat file")
		ImportCountriesDat(inputDirectory/"countries.dat.txt") match
		{
			case Success(countries) =>
				println(s"Inserted ${countries.size} new countries: [${countries.map { _.id }.mkString(", ")}]")
			case Failure(error) => error.printStackTrace()
		}
	}
}
