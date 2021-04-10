package vf.aviation.core.model.enumeration

import vf.aviation.core.model.enumeration.StandardAircraftWingType.{FixedWing, RotaryWing, Transforming, Wingless}

/**
 * A common trait for the standard aircraft types / categories used in this project by default
 * @author Mikko Hilpinen
 * @since 10.4.2021, v0.1
 */
trait StandardAircraftCategory
{
	/**
	 * Database id of this aircraft category
	 */
	val id: Int
	
	/**
	 * @return Wing type of this aircraft category
	 */
	def wingType: StandardAircraftWingType
}

object StandardAircraftCategory
{
	/**
	 * A normal fixed-wing airplane
	 */
	case object Airplane extends StandardAircraftCategory
	{
		override val id = 1
		override def wingType = FixedWing
	}
	
	/**
	 * A glider with some sort of power supply / motor
	 */
	case object PoweredGlider extends StandardAircraftCategory
	{
		override val id = 2
		override def wingType = FixedWing
	}
	
	/**
	 * A glider with no power supply / motor (E.g. sailplane)
	 */
	case object NonPoweredGlider extends StandardAircraftCategory
	{
		override val id = 3
		override def wingType = FixedWing
	}
	
	/**
	 * An aircraft with fixed horizontal rotors that uses wing rotation for lift
	 */
	case object Helicopter extends StandardAircraftCategory
	{
		override val id = 4
		override def wingType = RotaryWing
	}
	
	/**
	 * A hybrid between airplane and a helicopter that uses non-powered horizontally rotating wings for lift
	 */
	case object Gyroplane extends StandardAircraftCategory
	{
		override val id = 5
		override def wingType = RotaryWing
	}
	
	/**
	 * An airplane that is able to tilt its rotors upwards for to perform like a helicopter
	 */
	case object TiltRotor extends StandardAircraftCategory
	{
		override val id = 6
		override def wingType = FixedWing
	}
	
	/**
	 * An airplane that is able to tilt its wings (including rotors) to point upwards instead of forward
	 */
	case object TiltWing extends StandardAircraftCategory
	{
		override val id = 7
		override def wingType = Transforming
	}
	
	/**
	 * A powered airplane that uses gasses for lift (usually with horizontal thrust motors)
	 */
	case object Airship extends StandardAircraftCategory
	{
		override val id = 8
		override def wingType = Wingless
	}
	
	/**
	 * A non-powered airplane that uses gasses for lift
	 */
	case object Balloon extends StandardAircraftCategory
	{
		override val id = 9
		override def wingType = Wingless
	}
	
	/**
	 * An aircraft that combines aerodynamics (fixed wings) with gasses for lift
	 */
	case object HybridAirship extends StandardAircraftCategory
	{
		override val id = 10
		override def wingType = FixedWing
	}
}