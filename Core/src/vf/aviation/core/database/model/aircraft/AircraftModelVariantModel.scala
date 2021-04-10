package vf.aviation.core.database.model.aircraft

import utopia.flow.generic.ValueConversions._
import utopia.genesis.util.Distance
import utopia.vault.database.Connection
import utopia.vault.model.immutable.StorableWithFactory
import utopia.vault.sql.Insert
import vf.aviation.core.database.factory.aircraft.AircraftModelVariantFactory
import vf.aviation.core.model.cached.{Speed, Weight}
import vf.aviation.core.model.partial.aircraft.AircraftModelVariantData
import vf.aviation.core.model.stored.aircraft.AircraftModelVariant

import java.time.Year

object AircraftModelVariantModel
{
	// COMPUTED ---------------------------
	
	/**
	 * @return Factory used by this model
	 */
	def factory = AircraftModelVariantFactory
	/**
	 * @return Table used by this model
	 */
	def table = factory.table
	
	
	// OTHER    --------------------------
	
	/**
	 * @param data Aircraft model variant data
	 * @return A mode matching that data
	 */
	def apply(data: AircraftModelVariantData): AircraftModelVariantModel = apply(None, Some(data.modelId),
		Some(data.manufacturerId), Some(data.name), data.manufacturerCode, data.modelCode, data.seriesCode,
		data.designGroupId, data.wingSpan, data.length, data.tailHeight, data.wheelBase, data.mainGearWidth,
		data.numberOfSeats, data.maxTakeOffWeight, data.maxTaxiWeight, data.approachSpeedGroupId, data.approachSpeed,
		data.cruisingSpeed, data.manufactureStartedYear, data.manufactureEndedYear)
	
	/**
	 * Inserts a new aircraft model variant to the DB
	 * @param data Data to insert
	 * @param connection DB Connection (implicit)
	 * @return Newly inserted data
	 */
	def insert(data: AircraftModelVariantData)(implicit connection: Connection) =
		AircraftModelVariant(apply(data).insert().getInt, data)
	
	/**
	 * Inserts multiple new aircraft model variants to the DB
	 * @param data Data to insert
	 * @param connection DB Connection (implicit)
	 * @return Newly inserted data
	 */
	def insert(data: Seq[AircraftModelVariantData])(implicit connection: Connection) =
	{
		val ids = Insert(table, data.map { apply(_).toModel }).generatedIntKeys
		ids.zip(data).map { case (id, data) => AircraftModelVariant(id, data) }
	}
}

/**
 * Used for interacting with aircraft model variants in the DB
 * @author Mikko Hilpinen
 * @since 10.4.2021, v0.1
 */
case class AircraftModelVariantModel(id: Option[Int] = None, modelId: Option[Int] = None,
                                     manufacturerId: Option[Int] = None, name: Option[String] = None,
                                     manufacturerCode: Option[String] = None, modelCode: Option[String] = None,
                                     seriesCode: Option[String] = None, designGroupId: Option[Int] = None,
                                     wingSpan: Option[Distance] = None, length: Option[Distance] = None,
                                     tailHeight: Option[Distance], wheelBase: Option[Distance] = None,
                                     mainGearWidth: Option[Distance] = None, numberOfSeats: Option[Int] = None,
                                     maxTakeOffWeight: Option[Weight] = None, maxTaxiWeight: Option[Weight] = None,
                                     approachSpeedGroupId: Option[Int] = None, approachSpeed: Option[Speed] = None,
                                     cruisingSpeed: Option[Speed] = None, manufactureStartedYear: Option[Year] = None,
                                     manufactureEndedYear: Option[Year] = None)
	extends StorableWithFactory[AircraftModelVariant]
{
	override def factory = AircraftModelVariantModel.factory
	
	override def valueProperties = Vector("id" -> id, "modelId" -> modelId, "manufacturerId" -> manufacturerId,
		"name" -> name, "manufacturerCode" -> manufacturerCode, "modelCode" -> modelCode, "seriesCode" -> seriesCode,
		"designGroupId" -> designGroupId, "wingSpanFeet" -> wingSpan.map { _.toFeet },
		"lengthFeet" -> length.map { _.toFeet }, "tailHeightFeet" -> tailHeight.map { _.toFeet },
		"wheelBaseFeet" -> wheelBase.map { _.toFeet }, "mainGearWidthFeet" -> mainGearWidth.map { _.toFeet },
		"numberOfSeats" -> numberOfSeats, "maxTakeOffWeightPounds" -> maxTakeOffWeight.map { _.pounds },
		"maxTaxiWeightPounds" -> maxTaxiWeight.map { _.pounds }, "approachSpeedGroupId" -> approachSpeedGroupId,
		"approachSpeedKnots" -> approachSpeed.map { _.knots }, "cruisingSpeedKnots" -> cruisingSpeed.map { _.knots },
		"manufactureStartedYear" -> manufactureStartedYear.map { _.getValue },
		"manufactureEndedYear" -> manufactureEndedYear.map { _.getValue })
}
