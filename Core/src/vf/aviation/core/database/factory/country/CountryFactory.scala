package vf.aviation.core.database.factory.country

import utopia.flow.datastructure.immutable.{Constant, Model}
import utopia.flow.generic.ValueUnwraps._
import utopia.vault.nosql.factory.FromValidatedRowModelFactory
import vf.aviation.core.database.AviationTables
import vf.aviation.core.model.partial.country
import vf.aviation.core.model.stored.country.Country

/**
 * Used for reading country data from the DB
 * @author Mikko Hilpinen
 * @since 31.3.2021, v0.1
 */
object CountryFactory extends FromValidatedRowModelFactory[Country]
{
	// IMPLEMENTED  ---------------------------
	
	override def table = AviationTables.country
	
	override protected def fromValidatedModel(model: Model[Constant]) = Country(model("id"),
		country.CountryData(model("name"), model("worldRegionId"), model("isoCode"), model("dafifCode"),
			model("capitalId"), model("sovereigntyCountryId"), model("ended"), model("comment"),
			model("independent").boolean))
}
