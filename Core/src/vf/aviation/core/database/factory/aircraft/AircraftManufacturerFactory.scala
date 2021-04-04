package vf.aviation.core.database.factory.aircraft

import utopia.flow.datastructure.immutable.{Constant, Model}
import utopia.flow.generic.ValueUnwraps._
import utopia.vault.nosql.factory.FromValidatedRowModelFactory
import vf.aviation.core.database.AviationTables
import vf.aviation.core.model.partial.aircraft.AircraftManufacturerData
import vf.aviation.core.model.stored.aircraft.AircraftManufacturer

/**
 * Used for reading aircraft manufacturer data from DB
 * @author Mikko Hilpinen
 * @since 4.4.2021, v0.1
 */
object AircraftManufacturerFactory extends FromValidatedRowModelFactory[AircraftManufacturer]
{
	override def table = AviationTables.aircraftManufacturer
	
	override protected def fromValidatedModel(model: Model[Constant]) = AircraftManufacturer(model("id"),
		AircraftManufacturerData(model("icaoCode"), model("altCode"), model("countryId")))
}
