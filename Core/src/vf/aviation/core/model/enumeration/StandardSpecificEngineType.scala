package vf.aviation.core.model.enumeration

import vf.aviation.core.model.enumeration.StandardGenericEngineType.{AirbreathingJet, AirbreathingPropelling, OtherPropelling, OtherPropulsion, Piston}

/**
 * Common trait for the specific engine types used in this project by default
 * @author Mikko Hilpinen
 * @since 10.4.2021, v0.1
 */
trait StandardSpecificEngineType
{
	/**
	 * Database id of this engine type
	 */
	val id: Int
	
	/**
	 * @return Generic engine type group this type belongs to
	 */
	def genericType: StandardGenericEngineType
}

object StandardSpecificEngineType
{
	/**
	 * A turbine engine with a propeller on the front.
	 * See: https://en.wikipedia.org/wiki/Turboprop
	 */
	case object TurboProp extends StandardSpecificEngineType
	{
		override val id = 1
		override def genericType = AirbreathingPropelling
	}
	
	/**
	 * A turbine engine connected to a propeller via a shaft. Very similar to turboprop, except contains a shaft.
	 */
	case object TurboShaft extends StandardSpecificEngineType
	{
		override val id = 2
		override def genericType = AirbreathingPropelling
	}
	
	/**
	 * A turbine engine based on gas-based propulsion but without similar fan as in turbofan.
	 * Basically an older version of turbofan.
	 */
	case object TurboJet extends StandardSpecificEngineType
	{
		override val id = 3
		override def genericType = AirbreathingJet
	}
	
	/**
	 * A turbine based engine with gas-based propulsion and a fan on the front (but inside) of the
	 * turbine.
	 */
	case object TurboFan extends StandardSpecificEngineType
	{
		override val id = 4
		override def genericType = AirbreathingJet
	}
	
	/**
	 * An airbreathing turbine-based engine that operates fully based on the air intake.
	 * Requires forward motion before thrust can be generated.
	 */
	case object RamJet extends StandardSpecificEngineType
	{
		override val id = 5
		override def genericType = AirbreathingJet
	}
	
	/**
	 * A two-cycle piston engine (piston changes position 2 times in a cycle)
	 */
	case object TwoCycle extends StandardSpecificEngineType
	{
		override val id = 6
		override def genericType = Piston
	}
	
	/**
	 * A four-cycle piston engine (piston changes position 4 times in a cycle)
	 */
	case object FourCycle extends StandardSpecificEngineType
	{
		override val id = 7
		override def genericType = Piston
	}
	
	/**
	 * Electric rotor engines
	 */
	case object Electric extends StandardSpecificEngineType
	{
		override val id = 8
		override def genericType = OtherPropelling
	}
	
	/**
	 * Old-school gas-based rotary propeller engines
	 */
	case object Rotary extends StandardSpecificEngineType
	{
		override val id = 9
		override def genericType = OtherPropelling
	}
	
	/**
	 * Combustion and propulsion -based engines with no air intake
	 */
	case object Rocket extends StandardSpecificEngineType
	{
		override val id = 10
		override def genericType = OtherPropulsion
	}
}
