package vf.aviation.core.database.factory.aircraft

import utopia.vault.model.immutable.Result
import utopia.vault.nosql.factory.FromResultFactory
import utopia.vault.sql.JoinType
import utopia.vault.util.ErrorHandling
import vf.aviation.core.database.model.aircraft.AircraftManufacturerNameModel
import vf.aviation.core.model.combined.FullAircraftManufacturer

import scala.util.{Failure, Success}

/**
 * Used for reading aircraft manufacturer data from the DB, including manufacturer name data
 * @author Mikko Hilpinen
 * @since 5.4.2021, v0.1
 */
object FullAircraftManufacturerFactory extends FromResultFactory[FullAircraftManufacturer]
{
	// COMPUTED ------------------------------
	
	private def factory = AircraftManufacturerFactory
	private def nameModel = AircraftManufacturerNameModel
	
	private def nameTable = nameModel.table
	
	
	// IMPLEMENTED  --------------------------
	
	override def table = factory.table
	
	override def joinedTables = Vector(nameTable)
	
	override def joinType = JoinType.Left
	
	override def apply(result: Result) = result.grouped(table, nameTable)
		.flatMap { case (_, (manufacturerRow, nameRows)) =>
			// Parses manufacturer from the main row
			factory(manufacturerRow) match
			{
				case Success(manufacturer) =>
					// Parses names, if present
					val names = nameRows.flatMap { _(nameModel.nameAttName).string }
					Some(FullAircraftManufacturer(manufacturer, names))
				// Case: Failed to parse manufacturer data => uses error handler
				case Failure(error) =>
					ErrorHandling.modelParsePrinciple.handle(error)
					None
			}
		}.toVector
}
