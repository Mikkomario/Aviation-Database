package vf.aviation.core.model.stored.country

import java.time.{LocalDate, YearMonth}

/**
 * Represents a world area / world area code (WAC)
 * @author Mikko Hilpinen
 * @since 31.3.2021, v0.1
 * @param code The world area code
 * @param countryId Country to which this area belongs to
 * @param stateId Id of the state this area belongs to (if applicable)
 * @param name Name of this world area (if known)
 * @param started Year + month when this area was started (if known)
 * @param deprecatedAfter Date after which this area is no longer valid (None if still valid or unknown)
 */
case class WorldArea(code: Int, countryId: Int, stateId: Option[Int] = None, name: Option[String] = None,
                     started: Option[YearMonth] = None, deprecatedAfter: Option[LocalDate] = None)
