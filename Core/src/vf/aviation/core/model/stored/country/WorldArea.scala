package vf.aviation.core.model.stored.country

import java.time.{LocalDate, YearMonth}

/**
 * Represents a world area / world area code (WAC)
 * @author Mikko Hilpinen
 * @since 31.3.2021, v0.1
 * @param code The world area code
 * @param name Name of this world area
 * @param countryId Country to which this area belongs to
 * @param started Year + month when this area was started
 * @param stateId Id of the state this area belongs to (optional)
 * @param deprecatedAfter Date after which this area is no longer valid (None if still valid or unknown)
 */
case class WorldArea(code: Int, name: String, countryId: Int, started: YearMonth, stateId: Option[Int] = None,
                     deprecatedAfter: Option[LocalDate] = None)
