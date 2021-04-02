package vf.aviation.core.database.factory.station

import utopia.flow.datastructure.immutable.{Constant, Model}
import utopia.flow.generic.ValueUnwraps._
import utopia.genesis.shape.shape1D.Angle
import utopia.genesis.util.Distance
import utopia.vault.nosql.factory.FromValidatedRowModelFactory
import vf.aviation.core.database.AviationTables
import vf.aviation.core.model.cached.Coordinates
import vf.aviation.core.model.partial.station
import vf.aviation.core.model.stored
import vf.aviation.core.model.stored.station.Station

/**
 * Used for reading station data from the DB
 * @author Mikko Hilpinen
 * @since 2.4.2021, v0.1
 */
object StationFactory extends FromValidatedRowModelFactory[Station]
{
	override def table = AviationTables.station
	
	override protected def fromValidatedModel(model: Model[Constant]) = stored.station.Station(model("id"),
		station.StationData(model("name"), Coordinates(Angle.ofDegrees(model("latitudeNorth")),
			Angle.ofDegrees(model("longitudeEast"))), model("altitudeFeet").double.map(Distance.ofFeet),
			model("typeId"), model("dotId"), model("openFlightsId"), model("iataCode"), model("icaoCode"),
			model("cityId"), model("started"), model("closed"), model("isClosed")))
}
