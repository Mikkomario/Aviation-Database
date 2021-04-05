package vf.aviation.core.database.factory.aircraft

import utopia.flow.datastructure.immutable.{Constant, Model, Value}
import utopia.vault.nosql.factory.PossiblyMultiLinkedFactory
import vf.aviation.core.model.combined.FullAircraftManufacturer
import vf.aviation.core.model.stored.aircraft.AircraftManufacturerName

/**
 * Used for reading aircraft manufacturer data from the DB, including manufacturer name data
 * @author Mikko Hilpinen
 * @since 5.4.2021, v0.1
 */
object FullAircraftManufacturerFactory
	extends PossiblyMultiLinkedFactory[FullAircraftManufacturer, AircraftManufacturerName]
{
	// COMPUTED ------------------------------
	
	private def factory = AircraftManufacturerFactory
	
	
	// IMPLEMENTED  --------------------------
	
	override def table = factory.table
	
	override def childFactory = AircraftManufacturerNameFactory
	
	override def apply(id: Value, model: Model[Constant], children: Seq[AircraftManufacturerName]) =
		factory(model).map { FullAircraftManufacturer(_, children.map { _.name }.toVector) }
}
