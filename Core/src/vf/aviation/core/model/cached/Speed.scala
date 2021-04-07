package vf.aviation.core.model.cached

import vf.aviation.core.model.enumeration.SpeedUnit
import vf.aviation.core.model.enumeration.SpeedUnit.{KilometersPerHour, Knots, MilesPerHour}

object Speed
{
	/**
	 * @param amount Speed in knots
	 * @return That speed
	 */
	def knots(amount: Double) = apply(amount, Knots)
	/**
	 * @param amount Speed in kilometers per hour
	 * @return That speed
	 */
	def kilometersPerHour(amount: Double) = apply(amount, KilometersPerHour)
	/**
	 * @param amount Speed in miles per hour
	 * @return That speed
	 */
	def milesPerHour(amount: Double) = apply(amount, MilesPerHour)
}

/**
 * Represents an amount of speed
 * @author Mikko Hilpinen
 * @since 7.4.2021, v0.1
 */
case class Speed(amount: Double, unit: SpeedUnit) extends AmountWithUnit[SpeedUnit, Speed]
{
	// COMPUTED -----------------------------
	
	/**
	 * @return This speed in knots
	 */
	def knots = inUnit(Knots)
	/**
	 * @return This speed in kilometers per hour
	 */
	def kilometersPerHour = inUnit(KilometersPerHour)
	/**
	 * @return This speed in miles per hour
	 */
	def milesPerHour = inUnit(MilesPerHour)
	
	
	// IMPLEMENTED  -------------------------
	
	override protected def copy(newAmount: Double, newUnit: SpeedUnit) = Speed(newAmount, newUnit)
	
	override def repr = this
}
