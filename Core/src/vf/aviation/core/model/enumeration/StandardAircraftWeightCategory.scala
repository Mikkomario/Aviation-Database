package vf.aviation.core.model.enumeration

/**
 * A common trait for the aircraft weight categories used in this project by default
 * @author Mikko Hilpinen
 * @since 7.4.2021, v0.1
 */
trait StandardAircraftWeightCategory
{
	/**
	 * @return Database id of this category
	 */
	val id: Int
}

object StandardAircraftWeightCategory
{
	// COMPUTED -----------------------------------
	
	/**
	 * @return The smallest standard aircraft weight category
	 */
	def min = Light
	/**
	 * @return The largest standard aircraft weight category
	 */
	def max = Super
	
	
	// NESTED   ----------------------------------
	
	/**
	 * Smallest standard weight category of A/C less than 12 499 pounds
	 */
	case object Light extends StandardAircraftWeightCategory { val id = 1 }
	/**
	 * Second smallest standard weight category of A/C of 12 500 - 15 399 pounds
	 */
	case object LightPlus extends StandardAircraftWeightCategory { val id = 2 }
	/**
	 * Medium weight aircraft category: 15 400 - 19 999 pounds
	 */
	case object Medium extends StandardAircraftWeightCategory { val id = 3 }
	/**
	 * Heavier medium aircraft weight class: 20 000 - 40 999 pounds
	 */
	case object MediumPlus extends StandardAircraftWeightCategory { val id = 4 }
	/**
	 * Relatively heavy aircraft weight class: 41 000 - 299 999 pounds
	 */
	case object SemiHeavy extends StandardAircraftWeightCategory { val id = 5 }
	/**
	 * Heavy weight aircraft weight class: 300 000 - 999 999 pounds
	 */
	case object Heavy extends StandardAircraftWeightCategory { val id = 6 }
	/**
	 * Extreme aircraft weight class with over 1 million pounds of weight.
	 * At the time of writing (2021) there are only a couple of models (E.g. Beluga XL)
	 * with this weight class.
	 */
	case object Super extends StandardAircraftWeightCategory { val id = 7 }
}
