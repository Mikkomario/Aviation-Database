package vf.aviation.input.test

import utopia.flow.generic.DataType
import utopia.flow.util.FileExtensions._
import utopia.vault.database.Connection
import utopia.vault.sql.Delete
import vf.aviation.core.database.{AviationTables, ConnectionPool}
import vf.aviation.core.util.Globals._
import vf.aviation.input.controller.{ImportAirportsDat, ImportCountriesDat, ImportMasterCord, ImportWacCountryState}

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
	// Specifies correct character sets for the database connections
	Connection.modifySettings { _.copy(charsetName = "UTF-8", charsetCollationName = "utf8_general_ci") }
	
	val inputDirectory: Path = "Data-Input/input"
	
	/*
	No match could be found for country name 'South Georgia and the Islands'
	South Georgia and South Sandwich Is. == South Georgia and the Islands
	Full name is "South Georgia and the South Sandwich Islands"
	Also, Grytviken (Correct spelling) == Grytvyken (Incorrect spelling)
	 */
	
	ConnectionPool { implicit connection =>
		// Deletes pre-existing data
		println("Deleting old data")
		connection(Delete(AviationTables.worldRegion))
		connection(Delete(AviationTables.country))
		connection(Delete(AviationTables.station))
		
		// Imports WAC_COUNTRY_STATE document
		println("Starting world area code document processing")
		ImportWacCountryState(inputDirectory/"846163630_T_WAC_COUNTRY_STATE.csv").flatMap { _ =>
			// Imports countries.dat document
			println("Starting countries.dat document processing")
			ImportCountriesDat(inputDirectory/"countries.dat.txt")
		}.flatMap { newCountries =>
			if (newCountries.nonEmpty)
				println(s"Imported ${newCountries.size} new countries from countries.dat (id ${
					newCountries.head.id}+)")
			// Imports MASTER_CORD document
			println("Starting airport + city document processing")
			ImportMasterCord(inputDirectory/"846163630_T_MASTER_CORD.csv")
		}.flatMap { _ =>
			// Imports airports.dat document
			println("Starting airports.dat document processing")
			ImportAirportsDat(inputDirectory/"airports-extended.dat.csv")
		} match
		{
			case Success(_) => println("Finished importing data")
			case Failure(error) => error.printStackTrace()
		}
	}
}
