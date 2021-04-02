package vf.aviation.core.database.model.country

import utopia.flow.generic.ValueConversions._
import utopia.flow.time.TimeExtensions._
import utopia.vault.database.Connection
import utopia.vault.model.immutable.StorableWithFactory
import utopia.vault.sql.Insert
import vf.aviation.core.database.factory.country.WorldAreaFactory
import vf.aviation.core.model.stored.country.WorldArea

import java.time.LocalDate

object WorldAreaModel
{
	// COMPUTED ------------------------
	
	/**
	 * @return Factory used by this model
	 */
	def factory = WorldAreaFactory
	/**
	 * @return Table used by this model
	 */
	def table = factory.table
	
	
	// OTHER    ------------------------
	
	/**
	 * @param area A world area
	 * @return A model matching that area
	 */
	def apply(area: WorldArea): WorldAreaModel = apply(Some(area.code), Some(area.countryId),
		area.stateId, area.name, area.started.map { _.firstDay }, area.deprecatedAfter)
	
	/**
	 * Inserts a new world area to the database
	 * @param area Area to insert
	 * @param connection DB Connection (implicit)
	 * @return The area
	 */
	def insert(area: WorldArea)(implicit connection: Connection) =
	{
		apply(area).insert()
		area
	}
	
	/**
	 * Inserts multiple world areas to the database
	 * @param areas Areas to insert
	 * @param connection DB Connection (implicit)
	 * @return The areas
	 */
	def insert[S <: Seq[WorldArea]](areas: S)(implicit connection: Connection) =
	{
		Insert(table, areas.map { apply(_).toModel })
		areas
	}
}

/**
 * Used for interacting with world areas in the database
 * @author Mikko Hilpinen
 * @since 31.3.2021, v0.1
 */
case class WorldAreaModel(code: Option[Int] = None, countryId: Option[Int] = None, stateId: Option[Int] = None,
                          name: Option[String] = None, started: Option[LocalDate] = None,
                          deprecatedAfter: Option[LocalDate] = None)
	extends StorableWithFactory[WorldArea]
{
	override def factory = WorldAreaModel.factory
	
	override def valueProperties = Vector("code" -> code, "name" -> name, "countryId" -> countryId,
		"started" -> started, "stateId" -> stateId, "deprecatedAfter" -> deprecatedAfter)
}
