package vf.aviation.core.database.factory.aircraft

import utopia.flow.datastructure.immutable.{Constant, Model}
import utopia.flow.generic.ValueUnwraps._
import utopia.flow.time.TimeExtensions._
import utopia.genesis.util.Distance
import utopia.vault.nosql.factory.FromValidatedRowModelFactory
import vf.aviation.core.database.AviationTables
import vf.aviation.core.model.cached.{Speed, Weight}
import vf.aviation.core.model.partial.aircraft.AircraftModelVariantData
import vf.aviation.core.model.stored.aircraft.AircraftModelVariant

/**
 * Used for reading aircraft model variant data from the DB
 * @author Mikko Hilpinen
 * @since 8.4.2021, v0.1
 */
object AircraftModelVariantFactory extends FromValidatedRowModelFactory[AircraftModelVariant]
{
	override def table = AviationTables.aircraftModelVariant
	
	override protected def fromValidatedModel(model: Model[Constant]) =
	{
		val altCode = model("manufacturerCode").string.flatMap { manufacturerCode =>
			model("modelCode").string.flatMap { modelCode =>
				model("seriesCode").string.map { manufacturerCode + modelCode + _ }
			}
		}
		AircraftModelVariant(model("id"),
			AircraftModelVariantData(model("modelId"), model("manufacturerId"), model("name"), altCode,
				model("designGroupId"), model("wingSpanFeet").double.map(Distance.ofFeet),
				model("lengthFeet").double.map(Distance.ofFeet),
				model("tailHeightFeet").double.map(Distance.ofFeet), model("wheelBaseFeet").double.map(Distance.ofFeet),
				model("mainGearWidthFeet").double.map(Distance.ofFeet), model("numberOfSeats"),
				model("maxTakeOffWeightPounds").double.map(Weight.pounds),
				model("maxTaxiWeightPounds").double.map(Weight.pounds), model("approachSpeedGroupId"),
				model("approachSpeedKnots").double.map(Speed.knots), model("cruisingSpeedKnots").double.map(Speed.knots),
				model("manufactureStartedYear").int.map { _.year }, model("manufactureEndedYear").int.map { _.year }))
	}
}
