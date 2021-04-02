package vf.aviation.core.database.access.single.station

import utopia.vault.database.Connection
import utopia.vault.nosql.access.SingleRowModelAccess
import vf.aviation.core.database.factory.station.StationFactory
import vf.aviation.core.database.model.StationModel
import vf.aviation.core.model.stored.station.Station

/**
 * Used for accessing individual stations in the database
 * @author Mikko Hilpinen
 * @since 2.4.2021, v0.1
 */
object DbStation extends SingleRowModelAccess[Station]
{
	// COMPUTED ----------------------------
	
	private def model = StationModel
	
	/**
	 * @return An access point to individual airport stations
	 */
	def airport = DbAirport
	
	
	// IMPLEMENTED  ------------------------
	
	override def factory = StationFactory
	
	override def globalCondition = None
	
	
	// NESTED   ----------------------------
	
	object DbAirport extends SingleRowModelAccess[Station]
	{
		// IMPLEMENTED  --------------------
		
		override val globalCondition = Some(DbStation.mergeCondition(model.airport))
		
		override def factory = DbStation.factory
		
		
		// OTHER    ------------------------
		
		/**
		 * @param iataCode Airport IATA code
		 * @param connection DB Connection (implicit)
		 * @return An airport with that iata code, if one was found
		 */
		def withIataCode(iataCode: String)(implicit connection: Connection) =
			find(model.withIataCode(iataCode).toCondition)
	}
}
