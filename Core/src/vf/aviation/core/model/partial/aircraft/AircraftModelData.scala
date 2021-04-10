package vf.aviation.core.model.partial.aircraft

import vf.aviation.core.model.enumeration.StandardAircraftWeightCategory

/**
 * Contains basic information about an aircraft model
 * @author Mikko Hilpinen
 * @since 7.4.2021, v0.1
 * @param environmentId Id of this model's landing environment type (E.g. Sea or land)
 * @param minWeightCategoryId Id of the smallest category of weights for aircrafts in this model (default = Light)
 * @param maxWeightCategory Id of the largest category of weights for aircrafts in this model (default = Super)
 * @param altCode A code representing this aircraft model (if known). Contains both manufacturer code (3 characters)
 *                and model-specific code (2 characters)
 * @param iataCode 3 character IATA-assigned code for this aircraft model (if known)
 * @param icaoCode 4 character ICAO-assigned code for this aircraft model (if known)
 * @param categoryId Id of the category / type of this aircraft group (if known)
 * @param wingTypeId Id of the wing type of this aircraft group (if known)
 * @param numberOfEngines Number of engines on these aircrafts (if known)
 * @param engineCategoryId Generic engine type used by this model (if known)
 * @param engineTypeId Specific engine type used by this model (if known)
 * @param airworthiness Airworthiness code assigned for this model (if known)
 * @param taxiwayDesignGroupCode Code of the taxiway design group this model belongs to (if known)
 */
case class AircraftModelData(environmentId: Int, minWeightCategoryId: Int = StandardAircraftWeightCategory.min.id,
                             maxWeightCategory: Int = StandardAircraftWeightCategory.max.id,
                             altCode: Option[String] = None, iataCode: Option[String] = None,
                             icaoCode: Option[String] = None, categoryId: Option[Int] = None,
                             wingTypeId: Option[Int] = None, numberOfEngines: Option[Int] = None,
                             engineCategoryId: Option[Int] = None, engineTypeId: Option[Int] = None,
                             airworthiness: Option[Char] = None, taxiwayDesignGroupCode: Option[String] = None)
{
	/**
	 * @return Alternative code for the aircraft manufacturer / manufacturer group
	 */
	def manufacturerCode: Option[String] = altCode.map { _.take(3) }
	
	/**
	 * @return Alternative code for this model within the associated manufacturer group
	 */
	def modelCode = altCode.map { _.drop(3) }
}
