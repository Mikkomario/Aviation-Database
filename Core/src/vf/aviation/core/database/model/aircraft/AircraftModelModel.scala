package vf.aviation.core.database.model.aircraft

import utopia.flow.generic.ValueConversions._
import utopia.vault.model.immutable.StorableWithFactory
import vf.aviation.core.database.factory.aircraft.AircraftModelFactory
import vf.aviation.core.model.stored.aircraft.AircraftModel

object AircraftModelModel
{
	/**
	 * @return Factory used by this model
	 */
	def factory = AircraftModelFactory
}

/**
 * Used for interacting with aircraft model data in DB
 * @author Mikko Hilpinen
 * @since 8.4.2021, v0.1
 */
case class AircraftModelModel(id: Option[Int] = None, environmentId: Option[Int] = None,
                              minWeightCategoryId: Option[Int] = None, maxWeightCategory: Option[Int] = None,
                              manufacturerCode: Option[String] = None, modelCode: Option[String] = None,
                              iataCode: Option[String] = None, icaoCode: Option[String] = None,
                              categoryId: Option[Int] = None, wingTypeId: Option[Int] = None,
                              numberOfEngines: Option[Int] = None, engineCategoryId: Option[Int] = None,
                              engineTypeId: Option[Int] = None, airworthiness: Option[Char] = None,
                              taxiwayDesignGroupCode: Option[String] = None)
	extends StorableWithFactory[AircraftModel]
{
	override def factory = AircraftModelModel.factory
	
	override def valueProperties = Vector("id" -> id, "environmentId" -> environmentId,
		"minWeightCategoryId" -> minWeightCategoryId, "maxWeightCategoryId" -> maxWeightCategory,
		"manufacturerCode" -> manufacturerCode, "modelCode" -> modelCode, "iataCode" -> iataCode,
		"icaoCode" -> icaoCode, "categoryId" -> categoryId, "wingTypeId" -> wingTypeId,
		"numberOfEngines" -> numberOfEngines, "engineCategoryId" -> engineCategoryId, "engineTypeId" -> engineTypeId,
		"airworthiness" -> airworthiness.map { _.toString }, "taxiwayDesignGroupCode" -> taxiwayDesignGroupCode)
}