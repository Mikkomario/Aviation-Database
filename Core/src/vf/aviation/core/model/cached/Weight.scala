package vf.aviation.core.model.cached

import vf.aviation.core.model.enumeration.WeightUnit
import vf.aviation.core.model.enumeration.WeightUnit.{Kilograms, Pounds}

object Weight
{
	/**
	 * @param amount Amount of weight in kilograms
	 * @return Amount as a weight
	 */
	def kilograms(amount: Double) = apply(amount, Kilograms)
	/**
	 * @param amount Amount of weight in pounds
	 * @return Amount as a weight
	 */
	def pounds(amount: Double) = apply(amount, Pounds)
}

/**
 * Represents an amount of weight
 * @author Mikko Hilpinen
 * @since 7.4.2021, v0.1
 */
case class Weight(amount: Double, unit: WeightUnit) extends AmountWithUnit[WeightUnit, Weight]
{
	// COMPUTED --------------------------------
	
	/**
	 * @return This weight in kilograms
	 */
	def kilograms = inUnit(Kilograms)
	/**
	 * @return This weight in pounds
	 */
	def pounds = inUnit(Pounds)
	
	
	// IMPLEMENTED  ----------------------------
	
	override def repr = this
	
	override protected def copy(newAmount: Double, newUnit: WeightUnit) = Weight(newAmount, newUnit)
}
