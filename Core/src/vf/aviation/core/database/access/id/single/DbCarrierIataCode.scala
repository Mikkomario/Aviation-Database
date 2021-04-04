package vf.aviation.core.database.access.id.single

import utopia.vault.database.Connection
import vf.aviation.core.database.factory.CarrierFactory
import vf.aviation.core.database.model.CarrierModel

/**
 * Used for accessing individual carrier IATA codes in DB
 * @author Mikko Hilpinen
 * @since 4.4.2021, v0.1
 */
object DbCarrierIataCode
{
	// COMPUTED -------------------------
	
	private def factory = CarrierFactory
	private def model = CarrierModel
	
	
	// OTHER    -------------------------
	
	/**
	 * @param code Carrier IATA code
	 * @return An access point to that carrier code in DB
	 */
	def apply(code: String) = DbSingleCarrierIataCode(code)
	
	
	// NESTED   -------------------------
	
	case class DbSingleCarrierIataCode(code: String)
	{
		private def condition = model.withIataCode(code).toCondition
		
		/**
		 * @param connection DB Connection (implicit)
		 * @return Whether this carrier iata code has already been registered
		 */
		def isDefined(implicit connection: Connection) = factory.exists(condition)
		/**
		 * @param connection DB Connection (implicit)
		 * @return Whether this carrier iata code hasn't yet been registered
		 */
		def isEmpty(implicit connection: Connection) = !isDefined
	}
}
