package vf.aviation.core.model.stored.country

import utopia.vault.model.template.Stored
import vf.aviation.core.model.partial.country.CountryData

/**
 * Represents a country that has been recorded to the database
 * @author Mikko Hilpinen
 * @since 31.3.2021, v0.1
 */
case class Country(id: Int, data: CountryData) extends Stored[CountryData, Int]
