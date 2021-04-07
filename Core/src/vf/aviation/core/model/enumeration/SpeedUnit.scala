package vf.aviation.core.model.enumeration

import utopia.flow.time.TimeExtensions._
import utopia.genesis.util.Distance
import utopia.genesis.util.DistanceUnit.{KiloMeter, Mile, NauticalMile}

import scala.concurrent.duration.FiniteDuration

/**
 * Common trait for used units of speed
 * @author Mikko Hilpinen
 * @since 7.4.2021, v0.1
 */
sealed trait SpeedUnit extends MeasurementUnit[SpeedUnit]
{
	// ABSTRACT -------------------------
	
	/**
	 * @return Distance covered in 'referenceDuration'
	 */
	def coveredDistance: Distance
	/**
	 * @return Duration in which 'coveredDistance' is covered
	 */
	def referenceDuration: FiniteDuration
	
	
	// IMPLEMENTED  --------------------
	
	// V = dx/dt
	override def conversionRatioTo(other: SpeedUnit) =
	{
		val distanceModifier = coveredDistance / other.coveredDistance
		val timeModifier = other.referenceDuration / referenceDuration
		distanceModifier * timeModifier
	}
}

object SpeedUnit
{
	/**
	 * A speed unit used in sailing and aviation. 1 nautical mile per hour.
	 */
	case object Knots extends SpeedUnit
	{
		override val coveredDistance = Distance(1.0, NauticalMile)
		
		override val referenceDuration = 1.hours
		
		override def fullName = "Knots"
		
		override def shortName = "kn"
	}
	
	/**
	 * Metric speed unit in travelling etc.
	 */
	case object KilometersPerHour extends SpeedUnit
	{
		override val coveredDistance = Distance(1.0, KiloMeter)
		
		override val referenceDuration = 1.hours
		
		override def fullName = "Kilometers per hour"
		
		override def shortName = "km/h"
	}
	
	/**
	 * Metric speed unit in some contexts (E.g. when measuring wind)
	 */
	case object MetresPerSecond extends SpeedUnit
	{
		override val coveredDistance = Distance.ofMeters(1.0)
		
		override val referenceDuration = 1.seconds
		
		override def fullName = "Metres per second"
		
		override def shortName = "m/s"
	}
	
	/**
	 * Imperial speed unit
	 */
	case object MilesPerHour extends SpeedUnit
	{
		override val coveredDistance = Distance(1.0, Mile)
		
		override val referenceDuration = 1.hours
		
		override def fullName = "Miles per hour"
		
		override def shortName = "mph"
	}
}
