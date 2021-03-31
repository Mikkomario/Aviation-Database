package vf.aviation.core.database.factory.country

import utopia.flow.datastructure.immutable.{Constant, Model}
import utopia.flow.generic.ValueUnwraps._
import utopia.vault.nosql.factory.FromValidatedRowModelFactory
import vf.aviation.core.database.AviationTables
import vf.aviation.core.model.partial.country.StateData
import vf.aviation.core.model.stored.country.State

/**
 * Used for reading state data from the DB
 * @author Mikko Hilpinen
 * @since 31.3.2021, v0.1
 */
object StateFactory extends FromValidatedRowModelFactory[State]
{
	override def table = AviationTables.state
	
	override protected def fromValidatedModel(model: Model[Constant]) = State(model("id"),
		StateData(model("name"), model("countryId"), model("isoCode"), model("fipsCode"), model("comment")))
}
