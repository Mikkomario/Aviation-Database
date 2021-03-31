package vf.aviation.core.database.factory.country

import utopia.flow.datastructure.immutable.{Constant, Model}
import utopia.flow.generic.ValueUnwraps._
import utopia.flow.time.TimeExtensions._
import utopia.vault.nosql.factory.FromValidatedRowModelFactory
import vf.aviation.core.database.AviationTables
import vf.aviation.core.model.partial.country.CityData
import vf.aviation.core.model.stored.country.City

/**
 * Used for reading city data from the DB
 * @author Mikko Hilpinen
 * @since 31.3.2021, v0.1
 */
object CityFactory extends FromValidatedRowModelFactory[City]
{
	override def table = AviationTables.city
	
	override protected def fromValidatedModel(model: Model[Constant]) = City(model("id"),
		CityData(model("name"), model("countryId"), model("marketId"), model("stateId"), model("worldAreaCode"),
			model("timeZone").double.map { _.hours }))
}
