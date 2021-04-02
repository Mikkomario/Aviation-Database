package vf.aviation.core.database.factory.station

import utopia.flow.datastructure.immutable.{Constant, Model}
import utopia.flow.generic.ValueUnwraps._
import utopia.vault.nosql.factory.FromValidatedRowModelFactory
import vf.aviation.core.database.AviationTables
import vf.aviation.core.model.partial.station.StationTypeData
import vf.aviation.core.model.stored.station
import vf.aviation.core.model.stored.station.StationType

/**
 * Used for reading station types from the DB
 * @author Mikko Hilpinen
 * @since 2.4.2021, v0.1
 */
object StationTypeFactory extends FromValidatedRowModelFactory[StationType]
{
	override def table = AviationTables.stationType
	
	override protected def fromValidatedModel(model: Model[Constant]) = station.StationType(model("id"),
		StationTypeData(model("name"), model("openFlightsCode")))
}
