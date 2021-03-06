package vf.aviation.core.model.partial.aircraft

import utopia.genesis.util.Distance
import vf.aviation.core.model.cached.{Speed, Weight}

import java.time.Year

/**
 * Contains basic information about a specific aircraft model variant by a specific manufacturer
 * @author Mikko Hilpinen
 * @since 7.4.2021, v0.1
 * @param modelId Id of the model that describes this A/C type
 * @param manufacturerId Id of the manufacturer of this A/C variant
 * @param name Name of this model/variant
 * @param altCode A 7-character code for this model variant where the 3 first characters match an aircraft
 *                manufacturer alt code, the next 2 match a model code within that manufacturer group and the
 *                last 2 are specific to this variant within that model type. (if known)
 * @param designGroupId Id of the aircraft design group this model belongs in (if known)
 * @param wingSpan Wing span of this aircraft type from the end of one wing to the end of the other (if known)
 * @param length Length of this aircraft type from nose to tail (if known)
 * @param tailHeight Height of the tip of the tail of this aircraft type (if known)
 * @param wheelBase Distance from the front of the front wheels to the back of the middle wheels (if known)
 * @param mainGearWidth Distance from the right edge of the right wheel to the left edge of the left wheel
 *                  in this model / A/C type (if known)
 * @param numberOfSeats Number of seats installed on this model / type (if known)
 * @param maxTakeOffWeight Maximum weight allowed for take-off on this A/C type (if known)
 * @param maxTaxiWeight Maximum weight allowed for taxiing (ground-based moving) of this A/C type (if known)
 * @param approachSpeedGroupId Id of the approach speed group this A/C type belongs to (if known)
 * @param approachSpeed Maximum speed at which this A/C type can approach an air field (if known)
 * @param cruisingSpeed Standard speed in air (if known)
 * @param manufactureStartedYear Year when the manufacturing of this model was started (if known)
 * @param manufactureEndedYear Year when the manufacturing of this model was ended (if known / if applicable)
 */
case class AircraftModelVariantData(modelId: Int, manufacturerId: Int, name: String, altCode: Option[String] = None,
                                    designGroupId: Option[Int] = None, wingSpan: Option[Distance] = None,
                                    length: Option[Distance] = None, tailHeight: Option[Distance] = None,
                                    wheelBase: Option[Distance] = None, mainGearWidth: Option[Distance] = None,
                                    numberOfSeats: Option[Int] = None,
                                    maxTakeOffWeight: Option[Weight] = None, maxTaxiWeight: Option[Weight] = None,
                                    approachSpeedGroupId: Option[Int] = None, approachSpeed: Option[Speed] = None,
                                    cruisingSpeed: Option[Speed] = None, manufactureStartedYear: Option[Year] = None,
                                    manufactureEndedYear: Option[Year] = None)
{
	/**
	 * @return An alternative code for this model variant's manufacturer / manufacturer group
	 */
	def manufacturerCode = altCode.map { _.take(3) }
	
	/**
	 * @return An alternative code for this variant's model type within the manufacturer group
	 */
	def modelCode = altCode.map { _.slice(3, 5) }
	
	/**
	 * @return An alternative code for this variant within this model type
	 */
	def seriesCode = altCode.map { _.drop(5) }
}
