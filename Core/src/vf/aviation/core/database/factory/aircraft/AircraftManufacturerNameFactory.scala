package vf.aviation.core.database.factory.aircraft

import utopia.flow.datastructure.immutable.{Constant, Model}
import utopia.flow.generic.ValueUnwraps._
import utopia.vault.nosql.factory.FromValidatedRowModelFactory
import vf.aviation.core.database.AviationTables
import vf.aviation.core.model.partial.aircraft.AircraftManufacturerNameData
import vf.aviation.core.model.stored.aircraft.AircraftManufacturerName

/**
 * Used for reading aircraft manufacturer name data from the DB
 * @author Mikko Hilpinen
 * @since 5.4.2021, v0.1
 */
object AircraftManufacturerNameFactory extends FromValidatedRowModelFactory[AircraftManufacturerName]
{
	override def table = AviationTables.aircraftManufacturerName
	
	override protected def fromValidatedModel(model: Model[Constant]) = AircraftManufacturerName(model("id"),
		AircraftManufacturerNameData(model("manufacturerId"), model("name")))
}
