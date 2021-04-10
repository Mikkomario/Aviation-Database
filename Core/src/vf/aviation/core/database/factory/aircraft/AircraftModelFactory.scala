package vf.aviation.core.database.factory.aircraft

import utopia.flow.datastructure.immutable.{Constant, Model}
import utopia.flow.generic.ValueUnwraps._
import utopia.vault.nosql.factory.FromValidatedRowModelFactory
import vf.aviation.core.database.AviationTables
import vf.aviation.core.model.partial.aircraft.AircraftModelData
import vf.aviation.core.model.stored.aircraft.AircraftModel

/**
 * Used for reading aircraft models from the DB
 * @author Mikko Hilpinen
 * @since 8.4.2021, v0.1
 */
object AircraftModelFactory extends FromValidatedRowModelFactory[AircraftModel]
{
	override def table = AviationTables.aircraftModel
	
	override protected def fromValidatedModel(model: Model[Constant]) =
	{
		val altCode = model("manufacturerCode").string.flatMap { manufacturerCode =>
			model("modelCode").string.map { manufacturerCode + _ }
		}
		AircraftModel(model("id"),
			AircraftModelData(model("environmentId"), model("minWeightCategoryId"), model("maxWeightCategoryId"),
				altCode, model("iataCode"), model("icaoCode"), model("categoryId"), model("wingTypeId"),
				model("numberOfEngines"), model("engineCategoryId"), model("engineTypeId"),
				model("airworthiness").string.flatMap { _.headOption }, model("taxiwayDesignGroupCode")))
	}
}
