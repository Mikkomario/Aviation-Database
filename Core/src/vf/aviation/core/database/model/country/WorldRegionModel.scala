package vf.aviation.core.database.model.country

import utopia.flow.generic.ValueConversions._
import utopia.vault.database.Connection
import utopia.vault.model.immutable.StorableWithFactory
import utopia.vault.sql.Insert
import vf.aviation.core.database.factory.country.WorldRegionFactory
import vf.aviation.core.model.stored.country.WorldRegion

object WorldRegionModel
{
	// COMPUTED --------------------------
	
	private def factory = WorldRegionFactory
	
	/**
	 * @return Table used by this model
	 */
	def table = factory.table
	
	
	// OTHER    --------------------------
	
	/**
	 * @param region World region
	 * @return a model matching that region
	 */
	def apply(region: WorldRegion): WorldRegionModel = apply(Some(region.id), Some(region.name))
	
	/**
	 * @param name World region name
	 * @return A new world region model
	 */
	def withName(name: String) = apply(name = Some(name))
	
	/**
	 * Inserts a new world region to the DB. Please make first sure the region doesn't already exist in the DB
	 * @param region The region to insert
	 * @param connection DB Connection (implicit)
	 * @return Newly inserted region
	 */
	def insert(region: WorldRegion)(implicit connection: Connection) =
	{
		apply(region).insert()
		region
	}
	
	/**
	 * Inserts multiple world regions at once. Please make first sure the regions don't already exist in the DB
	 * @param regions Regions to insert
	 * @param connection DB Connection (implicit)
	 * @return Newly inserted regions
	 */
	def insert[R <: Seq[WorldRegion]](regions: R)(implicit connection: Connection) =
	{
		Insert(table, regions.map { apply(_).toModel })
		regions
	}
}

/**
 * Used for interacting with world region data in the database
 * @author Mikko Hilpinen
 * @since 29.3.2021, v0.1
 */
case class WorldRegionModel(id: Option[Int] = None, name: Option[String] = None)
	extends StorableWithFactory[WorldRegion]
{
	override def factory = WorldRegionModel.factory
	
	override def valueProperties = Vector("id" -> id, "name" -> name)
}
