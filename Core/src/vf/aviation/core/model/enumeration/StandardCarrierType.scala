package vf.aviation.core.model.enumeration

/**
 * Common trait for the standard carrier type values used in this project
 * @author Mikko Hilpinen
 * @since 4.4.2021, v0.1
 */
trait StandardCarrierType
{
	/**
	 * Carrier type id matching this value
	 */
	val id: Int
}

object StandardCarrierType
{
	/**
	 * Carriers that transport passengers and have 60+ seats
	 */
	case object Commuter extends StandardCarrierType { val id = 1 }
	/**
	 * Carriers that don't transport passengers and have over 18 000 pounds of maximum payload
	 */
	case object AllCargo extends StandardCarrierType { val id = 2 }
	/**
	 * Carriers that have less than 60 seats and have a maximum payload less than 18 000 pounds
	 */
	case object SmallCertified extends StandardCarrierType { val id = 3 }
}