package vf.aviation.core.model.enumeration

/**
 * A common trait for the aircraft wing types used in this project by default
 * @author Mikko Hilpinen
 * @since 10.4.2021, v0.1
 */
trait StandardAircraftWingType
{
	/**
	 * Database id for this wing type
	 */
	val id: Int
}

object StandardAircraftWingType
{
	/**
	 * Fixed-wing aircrafts have static (horizontal) wings, like normal airplanes.
	 */
	case object FixedWing extends StandardAircraftWingType { val id = 1 }
	
	/**
	 * Rotary wing aircrafts, like helicopters and gyrocopters have a horizontal set of wings that rotate.
	 */
	case object RotaryWing extends StandardAircraftWingType { val id = 2 }
	
	/**
	 * Transforming wing aircrafts may change the shape or the alignment of their wings.
	 * For example, tilt-wing aircrafts may change their wings between horizontal and vertical alignments.
	 */
	case object Transforming extends StandardAircraftWingType { val id = 3 }
	
	/**
	 * Wingless aircrafts don't have wings but use some other method for lift. This includes
	 * aircrafts like balloons / blimps / airships, as well as parachutes, etc.
	 */
	case object Wingless extends StandardAircraftWingType { val id = 4 }
}