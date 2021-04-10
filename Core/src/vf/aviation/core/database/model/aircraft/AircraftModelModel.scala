package vf.aviation.core.database.model.aircraft

import utopia.flow.generic.ValueConversions._
import utopia.vault.database.Connection
import utopia.vault.model.immutable.StorableWithFactory
import utopia.vault.sql.Insert
import vf.aviation.core.database.factory.aircraft.AircraftModelFactory
import vf.aviation.core.model.partial.aircraft.AircraftModelData
import vf.aviation.core.model.stored.aircraft.AircraftModel

object AircraftModelModel
{
	// COMPUTED -------------------------------
	
	/**
	 * @return Factory used by this model
	 */
	def factory = AircraftModelFactory
	/**
	 * @return The table used by this model type
	 */
	def table = factory.table
	
	
	// OTHER    -------------------------------
	
	/**
	 * @param data Aircraft model data
	 * @return A model matching that data
	 */
	def apply(data: AircraftModelData): AircraftModelModel = apply(None, Some(data.environmentId),
		Some(data.minWeightCategoryId), Some(data.maxWeightCategory), data.manufacturerCode, data.modelCode,
		data.iataCode, data.icaoCode, data.categoryId, data.wingTypeId, data.numberOfEngines, data.engineCategoryId,
		data.engineTypeId, data.airworthiness, data.taxiwayDesignGroupCode)
	
	/**
	 * Inserts a new aircraft model to the database
	 * @param data Aircraft model data
	 * @param connection DB Connection (implicit)
	 * @return Newly inserted data
	 */
	def insert(data: AircraftModelData)(implicit connection: Connection) =
		AircraftModel(apply(data).insert().getInt, data)
	
	/**
	 * Inserts multiple new aircraft models to the DB
	 * @param data Data to insert
	 * @param connection DB Connection (implicit)
	 * @return Newly inserted data
	 */
	def insert(data: Seq[AircraftModelData])(implicit connection: Connection) =
	{
		val ids = Insert(table, data.map { apply(_).toModel }).generatedIntKeys
		ids.zip(data).map { case (id, data) => AircraftModel(id, data) }
	}
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