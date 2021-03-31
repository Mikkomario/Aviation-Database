package vf.aviation.core.database.factory.country

import utopia.flow.datastructure.immutable.{Constant, Model}
import utopia.flow.generic.ValueUnwraps._
import utopia.vault.nosql.factory.FromValidatedRowModelFactory
import vf.aviation.core.database.AviationTables
import vf.aviation.core.model.stored.country.WorldRegion

/**
 * Used for reading world region data from the DB
 * @author Mikko Hilpinen
 * @since 29.3.2021, v0.1
 */
object WorldRegionFactory extends FromValidatedRowModelFactory[WorldRegion]
{
	override def table = AviationTables.worldRegion
	
	override protected def fromValidatedModel(model: Model[Constant]) = WorldRegion(model("id"), model("name"))
}
