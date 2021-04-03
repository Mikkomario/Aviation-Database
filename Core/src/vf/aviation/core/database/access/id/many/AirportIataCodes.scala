package vf.aviation.core.database.access.id.many

import utopia.vault.database.Connection
import utopia.vault.sql.{Select, Where}
import vf.aviation.core.database.model.StationModel

/**
 * Used for accessing airport iata code data in the DB
 * @author Mikko Hilpinen
 * @since 3.4.2021, v0.1
 */
object AirportIataCodes
{
	// ATTRIBUTES   ---------------------
	
	// Targets airports and unspecified stations
	private val typeCondition = model.airport.toCondition || model.typeIdColumn.isNull
	private val column = model.iataCodeColumn
	
	
	// COMPUTED -------------------------
	
	private def model = StationModel
	private def table = model.table
	
	
	// OTHER    -------------------------
	
	/**
	 * Finds airport (and unassigned station) iata codes which start with the specified letter(s)
	 * @param str The beginning letters of iata codes
	 * @param connection DB Connection (implicit)
	 * @return Registered airport iata codes that start with the specified letters
	 */
	def startingWith(str: String)(implicit connection: Connection) =
		connection(Select(table, column) + Where(column.startsWith(str) && typeCondition))
			.rowValues.flatMap { _.string }
}
