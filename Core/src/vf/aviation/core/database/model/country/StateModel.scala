package vf.aviation.core.database.model.country

import utopia.flow.generic.ValueConversions._
import utopia.vault.database.Connection
import utopia.vault.model.immutable.StorableWithFactory
import utopia.vault.sql.Insert
import vf.aviation.core.database.factory.country.StateFactory
import vf.aviation.core.model.partial.country.StateData
import vf.aviation.core.model.stored.country.State

object StateModel
{
	// COMPUTED --------------------------
	
	/**
	 * @return Factory used by this model
	 */
	def factory = StateFactory
	/**
	 * @return The table used by this model
	 */
	def table = factory.table
	
	
	// OTHER    ------------------------
	
	/**
	 * @param data State data
	 * @return A model matching that data
	 */
	def apply(data: StateData): StateModel = apply(None, Some(data.name), Some(data.countryId), Some(data.isoCode),
		data.fipsCode, data.comment)
	
	/**
	 * Inserts a new state to the DB
	 * @param data Data to insert
	 * @param connection DB Connection (implicit)
	 * @return Newly inserted state
	 */
	def insert(data: StateData)(implicit connection: Connection) = State(apply(data).insert().getInt, data)
	
	/**
	 * Inserts multiple states to the DB
	 * @param data Data to insert
	 * @param connection DB Connection (implicit)
	 * @return Newly inserted states
	 */
	def insert(data: Seq[StateData])(implicit connection: Connection) =
	{
		val ids = Insert(table, data.map { apply(_).toModel }).generatedIntKeys
		ids.zip(data).map { case (id, data) => State(id, data) }
	}
}

/**
 * Used for interacting with state data in the database
 * @author Mikko Hilpinen
 * @since 31.3.2021, v0.1
 */
case class StateModel(id: Option[Int] = None, name: Option[String] = None, countryId: Option[Int] = None,
                      isoCode: Option[String] = None, fipsCode: Option[Int] = None,
                      comment: Option[String] = None)
	extends StorableWithFactory[State]
{
	override def factory = StateModel.factory
	
	override def valueProperties = Vector("id" -> id, "name" -> name, "countryId" -> countryId,
		"isoCode" -> isoCode, "fipsCode" -> fipsCode, "comment" -> comment)
}
