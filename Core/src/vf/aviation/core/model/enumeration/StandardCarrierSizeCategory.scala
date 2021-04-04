package vf.aviation.core.model.enumeration

/**
 * A common trait for all the default carrier size categories used in this project
 * @author Mikko Hilpinen
 * @since 4.4.2021, v0.1
 */
trait StandardCarrierSizeCategory
{
	/**
	 * Id of this carrier size category
	 */
	val id: Int
}

object StandardCarrierSizeCategory
{
	/**
	 * Small regional carriers. Up to 20 M$ of yearly revenue.
	 */
	case object Small extends StandardCarrierSizeCategory { val id = 1 }
	/**
	 * Large regional carriers. Up to 100 M$ of yearly revenue.
	 */
	case object Medium extends StandardCarrierSizeCategory { val id = 2 }
	/**
	 * National carriers. Up to 1000 M$ of yearly revenue.
	 */
	case object Large extends StandardCarrierSizeCategory { val id = 3 }
	/**
	 * Major carriers with over 1000 M$ yearly revenue.
	 */
	case object Major extends StandardCarrierSizeCategory { val id = 4 }
}
