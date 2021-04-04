package vf.aviation.core.model.enumeration

/**
 * A common trait for all default station types in this project
 * @author Mikko Hilpinen
 * @since 2.4.2021, v0.1
 */
trait StandardStationType
{
	/**
	 * @return Station type id of this type
	 */
	val id: Int
}

object StandardStationType
{
	/**
	 * Airports (id 1) are the primary station type used in this project
	 */
	case object Airport extends StandardStationType { val id = 1 }
	/**
	 * Additionally used station type for trains
	 */
	case object TrainStation extends StandardStationType { val id = 2 }
	/**
	 * Additionally used station type for ferries
	 */
	case object FerryTerminal extends StandardStationType { val id = 3 }
}