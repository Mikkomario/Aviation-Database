package vf.aviation.core.model.cached

import utopia.genesis.util.Arithmetic
import vf.aviation.core.model.enumeration.MeasurementUnit

/**
 * A common trait for amounts that are described by a specific unit of measure
 * @author Mikko Hilpinen
 * @since 7.4.2021, v0.1
 */
trait AmountWithUnit[U <: MeasurementUnit[U], Repr <: AmountWithUnit[U, Repr]] extends Arithmetic[Repr, Repr]
{
	// ABSTRACT --------------------------------
	
	/**
	 * @return Numeric value of this amount
	 */
	def amount: Double
	/**
	 * @return Unit describing this amount
	 */
	def unit: U
	
	/**
	 * @param newAmount New amount to assing
	 * @param newUnit New unit to assign
	 * @return A copy of this amount with that amount and unit
	 */
	protected def copy(newAmount: Double = amount, newUnit: U = unit): Repr
	
	
	// IMPLEMENTED  ----------------------------
	
	override def toString = s"$amount $unit"
	
	override def -(another: Repr) = copy(newAmount = amount - another.inUnit(unit))
	
	override def *(mod: Double) = copy(newAmount = amount * mod)
	
	override def +(another: Repr) = copy(newAmount = amount + another.inUnit(unit))
	
	
	// OTHER    --------------------------------
	
	/**
	 * @param unit A weight unit
	 * @return This weight amount in that unit
	 */
	def inUnit(unit: U) = amount * this.unit.conversionRatioTo(unit)
}
