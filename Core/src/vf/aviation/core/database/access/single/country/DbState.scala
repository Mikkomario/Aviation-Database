package vf.aviation.core.database.access.single.country

import utopia.vault.database.Connection
import utopia.vault.nosql.access.SingleRowModelAccess
import vf.aviation.core.database.factory.country.StateFactory
import vf.aviation.core.database.model.country.StateModel
import vf.aviation.core.model.stored.country.State

/**
 * Used for accessing individual country states in the database
 * @author Mikko Hilpinen
 * @since 2.4.2021, v0.1
 */
object DbState extends SingleRowModelAccess[State]
{
	// COMPUTED -------------------------------
	
	private def model = StateModel
	
	
	// IMPLEMENTED  ---------------------------
	
	override def factory = StateFactory
	
	override def globalCondition = None
	
	
	// OTHER    -------------------------------
	
	/**
	 * @param isoCode State ISO-code
	 * @param connection DB Connection (implicit)
	 * @return A state with that ISO-code, if present
	 */
	def withIsoCode(isoCode: String)(implicit connection: Connection) =
		find(model.withIsoCode(isoCode).toCondition)
}
