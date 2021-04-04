package vf.aviation.core.database.access.single

import utopia.vault.database.Connection
import utopia.vault.nosql.access.SingleRowModelAccess
import utopia.vault.sql.OrderBy
import vf.aviation.core.database.factory.CarrierFactory
import vf.aviation.core.database.model.CarrierModel
import vf.aviation.core.model.stored.Carrier

/**
 * Used for accessing individual carriers in DB
 * @author Mikko Hilpinen
 * @since 4.4.2021, v0.1
 */
object DbCarrier extends SingleRowModelAccess[Carrier]
{
	// ATTRIBUTES   -----------------------------
	
	private val activeOrder = OrderBy.descending(model.isClosedColumn)
	
	
	// COMPUTED ---------------------------------
	
	private def model = CarrierModel
	
	
	// IMPLEMENTED  -----------------------------
	
	override def factory = CarrierFactory
	
	override def globalCondition = None
	
	
	// OTHER    ---------------------------------
	
	/**
	 * @param code IATA-assigned carrier code
	 * @param connection DB Connection (implicit)
	 * @return Carrier with that code, if found
	 */
	def forIataCode(code: String)(implicit connection: Connection) =
		find(model.withIataCode(code).toCondition, Some(activeOrder))
	
	/**
	 * @param code ICAO-assigned carrier code
	 * @param connection DB Connection (implicit)
	 * @return Carrier with that code, if found
	 */
	def forIcaoCode(code: String)(implicit connection: Connection) =
		find(model.withIcaoCode(code).toCondition, Some(activeOrder))
}
