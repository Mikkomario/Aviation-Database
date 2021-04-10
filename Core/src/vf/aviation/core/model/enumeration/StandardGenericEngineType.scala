package vf.aviation.core.model.enumeration

/**
 * Common trait for generic engine types used in this project by default
 * @author Mikko Hilpinen
 * @since 10.4.2021, v0.1
 */
trait StandardGenericEngineType
{
	/**
	 * Database id of this engine type
	 */
	val id: Int
}

object StandardGenericEngineType
{
	/**
	 * Turbine-based engines which have an air intake where most of the forward motion comes from a
	 * propeller that is connected with the turbine.
	 */
	case object AirbreathingPropelling extends StandardGenericEngineType
	{
		override val id = 1
	}
	
	/**
	 * Turbine-based jet engines which have an air intake and use fuel combustion as a means of thrust
	 * (propulsion instead of pull)
	 */
	case object AirbreathingJet extends StandardGenericEngineType
	{
		override val id = 2
	}
	
	/**
	 * Piston-based (rotary) engines with no air intake / turbine
	 */
	case object Piston extends StandardGenericEngineType
	{
		override val id = 3
	}
	
	/**
	 * Rotary / propelling engines that are not turbine- nor piston-based. E.g. electric motor engines
	 */
	case object OtherPropelling extends StandardGenericEngineType
	{
		override val id = 4
	}
	
	/**
	 * Propulsion engines that are not turbine-based. E.g. rocket engines
	 */
	case object OtherPropulsion extends StandardGenericEngineType
	{
		override val id = 5
	}
}
