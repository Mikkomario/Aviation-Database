package vf.aviation.input.test

import utopia.flow.generic.DataType
import utopia.flow.util.FileExtensions._
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
	
	val inputDirectory: Path = "Data-Input/input"
	
	/* FIXME: Problems with non-standard city names
	Caused by: org.mariadb.jdbc.internal.util.dao.QueryException:
		Illegal mix of collations (latin1_swedish_ci,IMPLICIT) and (utf8_general_ci,COERCIBLE) for operation '<=>'
	Query is: SELECT * FROM `city` WHERE (`city`.`country_id` <=> ? AND `city`.`name` <=> ?) LIMIT ?,
		parameters [650,'Győr',1]
	 */
	// Possible fix: https://stackoverflow.com/questions/1008287/illegal-mix-of-collations-mysql-error
	// TODO: Also, "No match could be found for country name 'West Bank'" - Actually Israel (Jerusalem)
	
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
			ImportAirportsDat(inputDirectory/"airports.dat.txt")
		} match
		{
			case Success(_) => println("Finished importing data")
			case Failure(error) => error.printStackTrace()
		}
	}
}
