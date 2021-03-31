package vf.aviation.core.model.stored.country

import utopia.vault.model.template.Stored
import vf.aviation.core.model.partial.country.StateData

/**
 * Represents a country state recorded in the database
 * @author Mikko Hilpinen
 * @since 31.3.2021, v0.1
 */
case class State(id: Int, data: StateData) extends Stored[StateData, Int]
