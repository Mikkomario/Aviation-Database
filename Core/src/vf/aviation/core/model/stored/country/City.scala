package vf.aviation.core.model.stored.country

import utopia.vault.model.template.Stored
import vf.aviation.core.model.partial.country.CityData

/**
 * Represents a city that has been recorded in the database
 * @author Mikko Hilpinen
 * @since 31.3.2021, v0.1
 */
case class City(id: Int, data: CityData) extends Stored[CityData, Int]
