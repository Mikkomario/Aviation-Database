package vf.aviation.core.database.factory.country

import utopia.flow.datastructure.immutable.{Constant, Model}
import utopia.flow.generic.ValueUnwraps._
import utopia.flow.time.TimeExtensions._
import utopia.vault.nosql.factory.FromValidatedRowModelFactory
import vf.aviation.core.database.AviationTables
import vf.aviation.core.model.stored.country.WorldArea

/**
 * Used for reading world area data from the DB
 * @author Mikko Hilpinen
 * @since 31.3.2021, v0.1
 */
object WorldAreaFactory extends FromValidatedRowModelFactory[WorldArea]
{
	override def table = AviationTables.worldArea
	
	override protected def fromValidatedModel(model: Model[Constant]) =
		WorldArea(model("code"), model("name"), model("countryId"), model("started").getLocalDate.yearMonth,
			model("stateId"), model("deprecatedAfter"))
}
