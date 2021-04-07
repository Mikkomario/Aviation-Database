package vf.aviation.core.model.enumeration

/**
 * Common trait for all units
 * @author Mikko Hilpinen
 * @since 7.4.2021, v0.1
 * @tparam Repr Type of unit this unit compares to
 */
trait MeasurementUnit[-Repr]
{
	// ABSTRACT -------------------------------
	
	/**
	 * @return Full name of this unit
	 */
	def fullName: String
	/**
	 * @return Short way to write this unit
	 */
	def shortName: String
	
	/**
	 * @param other Another unit
	 * @return A multiplier that must be applied to the amount to convert from this unit to that unit
	 */
	def conversionRatioTo(other: Repr): Double
	
	
	// IMPLEMENTED  ----------------------------
	
	override def toString = shortName
}


