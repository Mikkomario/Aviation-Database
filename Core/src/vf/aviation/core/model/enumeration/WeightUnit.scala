package vf.aviation.core.model.enumeration

/**
 * Enumeration for different weight units that may be used
 * @author Mikko Hilpinen
 * @since 7.4.2021, v0.1
 */
sealed trait WeightUnit extends MeasurementUnit[WeightUnit]

object WeightUnit
{
	/**
	 * Kilograms (1000 grams). Often used with metric units.
	 */
	case object Kilograms extends WeightUnit
	{
		override def fullName = "Kilograms"
		
		override def shortName = "kg"
		
		override def conversionRatioTo(other: WeightUnit) = other match
		{
			case Kilograms => 1.0
			case Pounds => 2.20462262
		}
	}
	
	/**
	 * Pounds. Often used with imperial units.
	 */
	case object Pounds extends WeightUnit
	{
		override def fullName = "Pounds"
		
		override def shortName = "lbs"
		
		override def conversionRatioTo(other: WeightUnit) = other match
		{
			case Pounds => 1.0
			case Kilograms => 0.45359237
		}
	}
}
