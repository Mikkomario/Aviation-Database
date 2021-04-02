package vf.aviation.input.test

import utopia.flow.generic.DataType
import utopia.flow.util.FileExtensions._
import utopia.vault.sql.Delete
import vf.aviation.core.database.{AviationTables, ConnectionPool}
import vf.aviation.core.util.Globals._
import vf.aviation.input.controller.{ImportCountriesDat, ImportWacCountryState}

import java.nio.file.Path
import scala.util.{Failure, Success}

/**
 * Reads all input data in order
 * @author Mikko Hilpinen
 * @since 2.4.2021, v0.1
 */
object AllDataImportTest extends App
{
	DataType.setup()
	
	val inputDirectory: Path = "Data-Input/input"
	
	ConnectionPool { implicit connection =>
		// Deletes pre-existing data
		println("Deleting old data")
		connection(Delete(AviationTables.worldRegion))
		connection(Delete(AviationTables.country))
		
		// Imports WAC_COUNTRY_STATE document
		println("Starting world area code document processing")
		ImportWacCountryState(inputDirectory/"846163630_T_WAC_COUNTRY_STATE.csv") match
		{
			case Success(_) =>
				// Imports countries.dat document
				ImportCountriesDat(inputDirectory/"countries.dat.txt") match
				{
					case Success(newDatCountries) =>
						if (newDatCountries.nonEmpty)
							println(s"Imported ${newDatCountries.size} new countries from countries.dat (id ${
								newDatCountries.head.id}+)")
						println("Finished importing data")
					case Failure(error) =>
				}
			case Failure(error) => error.printStackTrace()
		}
	}
}
