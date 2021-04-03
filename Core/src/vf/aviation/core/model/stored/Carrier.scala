package vf.aviation.core.model.stored

import utopia.vault.model.template.Stored
import vf.aviation.core.model.partial.CarrierData

/**
 * Represents a carrier / airline stored in the database
 * @author Mikko Hilpinen
 * @since 4.4.2021, v0.1
 */
case class Carrier(id: Int, data: CarrierData) extends Stored[CarrierData, Int]
