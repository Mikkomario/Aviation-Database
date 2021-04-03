package vf.aviation.core.database.factory

import utopia.flow.datastructure.immutable.{Constant, Model}
import utopia.flow.generic.ValueUnwraps._
import utopia.flow.time.TimeExtensions._
import utopia.vault.nosql.factory.FromValidatedRowModelFactory
import vf.aviation.core.database.AviationTables
import vf.aviation.core.model.partial.CarrierData
import vf.aviation.core.model.stored.Carrier

/**
 * Used for reading carrier data from DB
 * @author Mikko Hilpinen
 * @since 4.4.2021, v0.1
 */
object CarrierFactory extends FromValidatedRowModelFactory[Carrier]
{
	override def table = AviationTables.carrier
	
	override protected def fromValidatedModel(model: Model[Constant]) = Carrier(model("id"),
		CarrierData(model("name"), model("alias"), model("callSign"), model("dotId"), model("openFlightsId"),
			model("iataCode"), model("icaoCode"), model("countryId"), model("worldAreaCode"), model("sizeCategoryId"),
			model("typeCategoryId"), model("started").localDate.map { _.yearMonth }, model("ended"),
			model("isClosed")))
}
